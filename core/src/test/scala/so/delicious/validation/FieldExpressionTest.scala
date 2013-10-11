package so.delicious.validation

import scala.language.experimental.macros
import org.scalatest.FreeSpec

class FieldExpressionTest extends FreeSpec {
  def toFieldExpression[T](expr: T) = macro FieldExpressionMacros.toFieldExpressionMacro[T]

  "toFieldExpression" in {
    object Testing {
      val a = this
      val b = this
      val c = 3

      val expr = toFieldExpression(a.b.a.a.c)
    }

    assert(Testing.expr.value === 3)
    assert(Testing.expr.components === Seq('a, 'b, 'a, 'a, 'c))
  }
}
