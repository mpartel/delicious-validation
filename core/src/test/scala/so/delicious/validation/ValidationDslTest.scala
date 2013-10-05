package so.delicious.validation

import org.scalatest.FreeSpec
import org.scalatest.Matchers

class ValidationDslTest extends FreeSpec with Matchers {
  // TODO: real test cases. For now we just check that this compiled.

  case class Foo(x: Int) extends Validated {
    x is "bad" when { x > 9000 }
    x must "be positive" thatIs { x > 0 }
    x must "be positive" meaning { x > 0 }
    x must "be positive" ie { x > 0 }
    x ~ "must be positive" ~ { x > 0 }
  }

  "syntax: must ... ~ lambda" is pending

  "syntax: is ... when lambda" is pending

  // ...
}
