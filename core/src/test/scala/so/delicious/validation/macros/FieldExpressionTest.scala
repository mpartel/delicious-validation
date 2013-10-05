package so.delicious.validation.macros

import org.scalatest.FreeSpec

class FieldExpressionTest extends FreeSpec {
  "toFieldExpression" in {
    object Testing {
      val a = this
      val b = this
      val c = 3

      val expr = FieldExpressionMacros.toFieldExpression(a.b.a.a.c)
    }

    assert(Testing.expr.value === 3)
    assert(Testing.expr.components === Seq('a, 'b, 'a, 'a, 'c))
  }
}
