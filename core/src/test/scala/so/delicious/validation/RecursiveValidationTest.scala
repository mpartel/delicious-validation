package so.delicious.validation

import org.scalatest.FreeSpec
import org.scalatest.Matchers

class RecursiveValidationTest extends FreeSpec with Matchers {
  class Base(val s: String) extends Validated {
    s ~ "must be all caps" ~ (s == s.toUpperCase)
  }

  class Deriv(subS: String, val n: Int, privN: Int) extends Base(subS) {
    n ~ "must be positive" ~ (n > 0)
    // TODO
  }

  "superclass validators are run before the subclass validators" in {
    new Deriv("asd", -5, 3).validationErrors should equal (List(
      ValidationError(List('s), "must be all caps", "asd"),
      ValidationError(List('n), "must be positive", -5)
    ))
  }

  // TODO: more

}
