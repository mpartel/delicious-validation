package so.delicious.validation

import scala.language.implicitConversions
import so.delicious.validation.macros.FieldExpression
import so.delicious.validation.macros.FieldExpressionAndMsg
import so.delicious.validation.macros.FieldExpressionImplicits

trait Validation extends DelayedInit with FieldExpressionImplicits {
  @transient protected[this] var _validationErrors = Seq.empty[ValidationError]

  def delayedInit(init: => Unit) {
    init

    // TODO: automatic subvalidation of subfields that extend Validation

    if (_validationErrors.isEmpty) {
      throw new ValidationException(_validationErrors)
    }
  }

  implicit class AfterDescription[T](expr: FieldExpressionAndMsg[T]) {
    def when(isBad: Boolean) {
      if (isBad) {
        _validationErrors :+= ValidationError(expr.expr.components, expr.msg, expr.value)
      }
    }

    def ~(isGood: Boolean) {
      if (!isGood) {
        _validationErrors :+= ValidationError(expr.expr.components, expr.msg, expr.value)
      }
    }
  }
}

case class Foo(x: Int) extends Validation {
  x ~ "is bad" when { x > 9000 }
  x ~ "must be positive" ~ { x > 0 }
}

class Svc

case class Xoox(a: Int)(implicit svc: Svc) {
  // use svc to validate
}

// Hmm... a macro can access implicits at its call site.
// So an extractor that supports an implicit arg list should be possible.

object Xoox {
  def f(x: Xoox) {
    x match {
      case Xoox(a) => ???
    }
  }
}
