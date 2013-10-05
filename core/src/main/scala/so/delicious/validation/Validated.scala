package so.delicious.validation

import scala.language.implicitConversions
import so.delicious.validation.macros._

/**
 * Base trait for validatable objects.
 */
trait Validated extends DelayedInit with FieldExpressionDslBase {
  @transient protected[this] var _validationErrors = List.empty[ValidationError]

  def delayedInit(init: => Unit) {
    subvalidated.map { case (sym, v) =>
      for (e <- v.validationErrors) {
        _validationErrors ::= e.inContext(sym)
      }
    }

    // Subobjects get validated first because
    // the container may be interested in their validity.

    init

    _validationErrors = _validationErrors.reverse
  }

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
  def validationErrors: Seq[ValidationError] = _validationErrors

  /**
   * Throws `ValidationException` if `!isValid`.
   */
  def throwIfInvalid() {
    if (!_validationErrors.isEmpty) {
      throw new ValidationException(_validationErrors)
    }
  }

  /**
   * The subobjects to validate.
   *
   * Defaults to all fields created from public constructor parameters
   * (i.e. either `val` parameters or case class parameters).
   *
   * May be overridden.
   */
  protected[this] def subvalidated: Seq[(Symbol, Validated)] = {
    ConstructorParams(this).flatMap { case (sym, mirror) =>
      mirror.get match {
        case v: Validated => Some(sym, v)
        case _ => None
      }
    }
  }

  private[this] def addValidationError(e: ValidationError) {
    _validationErrors ::= e
  }

  private[this] def addValidationError[T](expr: FieldExpression[T], msg: String) {
    _validationErrors ::= ValidationError(expr.components, msg, expr.value)
  }


  type W[T] = AfterFieldExpression[T]

  implicit class AfterFieldExpression[A](expr: FieldExpression[A]) {
    def ~(msg: String) = new AfterTilde(expr, msg)
    def must(msg: String) = new AfterMustWord(expr, msg)  // maybe remove
    def is(msg: String) = new AfterIsWord(expr, msg)      // maybe remove
  }

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

  class AfterMustWord[A](expr: FieldExpression[A], msg: String) {
    def thatIs(isGood: => Boolean) {
      if (!isGood) {
        addValidationError(expr, msg)
      }
    }
    def meaning(isGood: => Boolean) { thatIs(isGood) }
    def ie(isGood: => Boolean) { thatIs(isGood) }
  }

  class AfterIsWord[A](expr: FieldExpression[A], msg: String) {
    def when(isBad: => Boolean) {
      if (isBad) {
        addValidationError(expr, msg)
      }
    }
  }
}
