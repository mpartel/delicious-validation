package so.delicious.validation

import scala.language.experimental.macros
import scala.language.implicitConversions

class ValidatorDsl[T](implicit val staticValidatorInfo: StaticValidatorInfo[T]) extends Validator[T] {
  private type Rule = ValidationDependencyInjector => Option[ValidationError]
  private[this] var rules = List.empty[Rule]

  def validate(obj: T)(implicit di: ValidationDependencyInjector = ValidationDependencyInjector.empty) = {
    rules.flatMap(_.apply(di)).reverse
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
      rules ::= { injector => if (!isGood) Some(ValidationError(expr.components.map(_.toString), msg, expr.value)) else None }
    }

    def when(isBad: => Boolean) {
      rules ::= { injector => if (isBad) Some(ValidationError(expr.components.map(_.toString), msg, expr.value)) else None }
    }
  }
}
