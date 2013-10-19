package so.delicious.validation

import scala.language.experimental.macros
import scala.language.implicitConversions
import scala.reflect.runtime.{universe => runtimeUniverse}

/**
 * Base trait for validatable objects.
 */
trait Validated {
  @transient protected[this] var _validationErrors = List.empty[ValidationError]

  validateSubobjects(subobjectsToValidate)

  /**
   * Whether the object has no validation errors.
   *
   * It is legal (but not necessarily good style) to call in the constructor,
   * during validation, to check whether any validation errors have occurred so far.
   */
  def isValid: Boolean = _validationErrors.isEmpty

  /**
   * The object's validation errors.
   */
  def validationErrors: Seq[ValidationError] = _validationErrors.reverse

  /**
   * Throws `ValidationException` if `!isValid`.
   */
  def throwIfInvalid() {
    if (!_validationErrors.isEmpty) {
      throw new ValidationException(_validationErrors)
    }
  }

  /**
   * Grabs a validation dependency from an implicit `ValidationDependencyInjector`.
   */
  def injected[T : runtimeUniverse.TypeTag](implicit injector: ValidationDependencyInjector) = injector.get[T]

  /**
   * Validates the given list of subpath-subobject pairs and adds the errors to
   * the current object's list of validation errors.
   *
   * By default, called on `subobjectsToValidate` in the `Validated` trait's constructor.
   *
   * May be overridden.
   */
  protected[this] def validateSubobjects(subobjects: Seq[(List[String], Validated)]) {
    subobjects.map { case (ctx, v) =>
      for (e <- v.validationErrors) {
        _validationErrors ::= e.inContext(ctx)
      }
    }
  }

  /**
   * The subobjects to validate during the `Validated` trait's constructor.
   *
   * Defaults to all public parameters in the primary constructor's first parameter list.
   *
   * May be overridden.
   */
  protected[this] def subobjectsToValidate: Seq[(List[String], Validated)] = {
    ConstructorParams(this).flatMap { case (sym, value) =>
      castToValidated(List(sym.name), value).map {
        case (reverseCtx, v) => (reverseCtx.reverse, v)
      }
    }.toSeq
  }

  /**
   * Recursively converts an object to `Iterable[(List[String], Validated)]` if possible.
   *
   * By default, arguments of the following runtime types are traversed:
   * - `Validated`
   * - `Some[Validated]`
   * - `Map[_, Validated]`
   * - `Iterable[Validated]`
   *
   * Called by the default implementation of `subobjectsToValidate`.
   */
  protected[this] def castToValidated(reverseCtx: List[String], value: Any): Iterable[(List[String], Validated)] = {
    value match {
      case v: Validated => List(reverseCtx -> v)
      case Some(v: Validated) => List(reverseCtx -> v)
      case map: Map[_, _] => map.flatMap {
        case (k, v) => castToValidated(k.toString :: reverseCtx, v)
      }
      case iterable: Iterable[_] => iterable.zipWithIndex.flatMap {
        case (a, i) => castToValidated(i.toString :: reverseCtx, a)
      }
      case _ => List.empty
    }
  }

  private[this] def addValidationError(e: ValidationError) {
    _validationErrors ::= e
  }

  private[this] def addValidationError[T](expr: FieldExpression[T], msg: String) {
    _validationErrors ::= ValidationError(expr.components.map(_.name), msg, expr.value)
  }


  /** DSL implementation detail. */
  implicit class AfterFieldExpression[A](expr: FieldExpression[A]) {
    def ~(msg: String) = new AfterTilde(expr, msg)
  }

  /** DSL implementation detail. */
  implicit def toAfterFieldExpression[T](expr: T)(
    implicit wrap: FieldExpression[T] => AfterFieldExpression[T]
  ): AfterFieldExpression[T] = macro FieldExpressionMacros.toFieldExpressionWithWrapperMacro[T, AfterFieldExpression]


  /** DSL implementation detail. */
  class AfterTilde[A](expr: FieldExpression[A], msg: String) {
    def ~(isGood: => Boolean) {
      if (!isGood) {
        addValidationError(expr, msg)
      }
    }

    def when(isBad: => Boolean) {
      if (isBad) {
        addValidationError(expr, msg)
      }
    }
  }
}
