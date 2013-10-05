package so.delicious.validation

import org.scalatest.FreeSpec
import org.scalatest.Matchers

class BasicValidationTest extends FreeSpec with Matchers {
  case class Foo(x: Int, y: String) extends Validated {
    x ~ "must be greater than the length of y" ~ (x > y.length)
    x ~ "must be no more than 100" ~ (x < 100)
  }

  "when the first validation fails" in {
    Foo(2, "asd").validationErrors should equal (List(
      ValidationError(List('x), "must be greater than the length of y", 2)
    ))
  }

  "when the second validation fails" in {
    Foo(9001, "asd").validationErrors should equal (List(
      ValidationError(List('x), "must be no more than 100", 9001)
    ))
  }

  "when both validations fail" in {
    Foo(101, "asd" * 100).validationErrors should equal (List(
      ValidationError(List('x), "must be greater than the length of y", 101),
      ValidationError(List('x), "must be no more than 100", 101)
    ))
  }

  "when neither validation fails" in {
    Foo(10, "asd").validationErrors should be (empty)
  }
}
