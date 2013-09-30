package so.delicious.validation

import so.delicious.validation.macros.FieldExpression

trait Validation extends DelayedInit {
  @transient protected[this] var _validationErrors = Seq.empty[ValidationError]

  def delayedInit(init: => Unit) {
    init

    // TODO: automatic subvalidation of subfields that extend Validation

    if (_validationErrors.isEmpty) {
      throw new ValidationException(_validationErrors)
    }
  }

  def field[T](expr: FieldExpression[T]) = new AfterField(expr)

  class AfterField[T](expr: FieldExpression[T]) {
    def ~(message: String) = new AfterDescription(expr, message)

    // TODO: reusable validators
  }

  class AfterDescription[T](expr: FieldExpression[T], message: String) {
    def when(isBad: Boolean) {
      if (isBad) {
        _validationErrors :+= ValidationError(expr.components, message, expr.value)
      }
    }

    def ~(isGood: => Boolean) {
      if (!isGood) {
        _validationErrors :+= ValidationError(expr.components, message, expr.value)
      }
    }
  }
}

case class Foo(x: Int) extends Validation {
  field(x) ~ "is bad" when { x > 9000 }
  field(x) ~ "must be positive" ~ { x > 0 }
}
