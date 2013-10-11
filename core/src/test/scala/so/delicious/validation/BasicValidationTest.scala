package so.delicious.validation

import org.scalatest.FreeSpec
import org.scalatest.Matchers

class BasicValidationTest extends FreeSpec with Matchers {
  case class Foo(x: Int, y: String) extends Validated {
    x ~ "must be greater than the length of y" ~ (x > y.length)
    x ~ "must be no more than 100" ~ (x < 100)
    x ~ "is too small" when (x < 10)
  }

  "when the first validation fails" in {
    Foo(12, "asd" * 100).validationErrors should equal (List(
      ValidationError(List("x"), "must be greater than the length of y", 12)
    ))
  }

  "when the second validation fails" in {
    Foo(9001, "asd").validationErrors should equal (List(
      ValidationError(List("x"), "must be no more than 100", 9001)
    ))
  }

  "when two validations fail" in {
    Foo(101, "asd" * 100).validationErrors should equal (List(
      ValidationError(List("x"), "must be greater than the length of y", 101),
      ValidationError(List("x"), "must be no more than 100", 101)
    ))
  }

  "when a 'when' validation fails" in {
    Foo(5, "asd").validationErrors should equal (List(
      ValidationError(List("x"), "is too small", 5)
    ))
  }

  "when neither validation fails" in {
    Foo(10, "asd").validationErrors should be (empty)
  }
}
