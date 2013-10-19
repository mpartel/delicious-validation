package so.delicious.validation

import org.scalatest.FreeSpec
import org.scalatest.Matchers

/*
object RecursiveValidationTest {
  case class Positive(n: Int) extends Validated {
    n ~ "must be positive" ~ (n > 0)
  }

  case class SmallPair(x: Int, y: Int) extends Validated {
    this ~ "has a total greater than 100" when (x + y > 100)
  }

  case class TwoPositives(a: Positive, b: Positive) extends Validated

  case class MaybePositive(a: Option[Positive]) extends Validated

  case class ManyPositives(a: Iterable[Positive]) extends Validated

  case class ManyManyPositives(a: Iterable[Iterable[Positive]]) extends Validated

  case class StringMapOfPositives(a: Map[String, Positive]) extends Validated

  case class IntMapOfPositives(a: Map[Int, Positive]) extends Validated

  case class LongMapOfPositives(a: Map[Long, Positive]) extends Validated

  case class TwoSmallPairs(a: SmallPair, b: SmallPair) extends Validated {
    b ~ "has a total greater than 50" when (b.x + b.y > 50)
  }
}

class RecursiveValidationTest extends FreeSpec with Matchers {
  import RecursiveValidationTest._

  "superclass validators are run before the subclass validators" in {
    var magic = 10
    class Base(val s: String) extends Validated {
      s ~ "must be all caps" ~ { magic += 1; s == s.toUpperCase }
    }

    case class Deriv(ds: String, val n: Int) extends Base(ds) {
      n ~ "must be positive" ~ { magic *= 2; n > 0 }
    }

    new Deriv("asd", -5).validationErrors should equal (List(
      ValidationError(List("s"), "must be all caps", "asd"),
      ValidationError(List("n"), "must be positive", -5)
    ))

    magic should be (22)  // It would be 21 if the order was wrong
  }

  "fields that are Validatable are autovalidated" in {
    TwoPositives(Positive(-1), Positive(-2)).validationErrors should equal (List(
      ValidationError(List("a", "n"), "must be positive", -1),
      ValidationError(List("b", "n"), "must be positive", -2)
    ))
  }

  "fields that are Option[Validatable] are autovalidated" in {
    MaybePositive(None) should be ('valid)
    MaybePositive(Some(Positive(-5))).validationErrors should equal (List(
      ValidationError(List("a", "n"), "must be positive", -5)
    ))
  }

  "fields that are Iterable[Validatable] are autovalidated" in {
    ManyPositives(Seq.empty) should be ('valid)
    ManyPositives(Seq(Positive(3), Positive(-7), Positive(4), Positive(-9))).validationErrors should equal (List(
      ValidationError(List("a", "1", "n"), "must be positive", -7),
      ValidationError(List("a", "3", "n"), "must be positive", -9)
    ))
  }

  "fields that are Iterable[Iterable[Validatable]] are autovalidated" in {
    val obj = ManyManyPositives(
      Seq(
        Seq(Positive(3), Positive(-7)),
        Seq(Positive(-4), Positive(9))
      )
    )
    obj.validationErrors should equal (List(
      ValidationError(List("a", "0", "1", "n"), "must be positive", -7),
      ValidationError(List("a", "1", "0", "n"), "must be positive", -4)
    ))
  }

  "fields that are Map[String, Validatable] are autovalidated" in {
    val obj = StringMapOfPositives(Map(
      "one" -> Positive(123),
      "two" -> Positive(-456),
      "three" -> Positive(789)
    ))
    obj.validationErrors should equal (List(
      ValidationError(List("a", "two", "n"), "must be positive", -456)
    ))
  }

  "fields that are Map[Int, Validatable] are autovalidated" in {
    val obj = IntMapOfPositives(Map(
      11 -> Positive(123),
      22 -> Positive(-456),
      33 -> Positive(789)
    ))
    obj.validationErrors should equal (List(
      ValidationError(List("a", "22", "n"), "must be positive", -456)
    ))
  }

  "fields that are Map[Long, Validatable] are autovalidated" in {
    val obj = LongMapOfPositives(Map(
      11.toLong -> Positive(123),
      22.toLong -> Positive(-456),
      33.toLong -> Positive(789)
    ))
    obj.validationErrors should equal (List(
      ValidationError(List("a", "22", "n"), "must be positive", -456)
    ))
  }

  "subfields with a `this` validator don't add to the context" in {
    val first = SmallPair(99, 99)
    val second = SmallPair(10, 20)
    val obj = TwoSmallPairs(
      first,
      second
    )
    obj.validationErrors should equal (List(
      ValidationError(List("a"), "has a total greater than 100", first)
    ))
    (obj.validationErrors(0).value.asInstanceOf[SmallPair]) should be theSameInstanceAs (first)
  }

  "subfield errors come before local errors" in {
    val obj = TwoSmallPairs(
      SmallPair(5, 10),
      SmallPair(99, 99)
    )
    obj.validationErrors should equal (List(
      ValidationError(List("b"), "has a total greater than 100", SmallPair(99, 99)),
      ValidationError(List("b"), "has a total greater than 50", SmallPair(99, 99))
    ))

    val obj2 = TwoSmallPairs(
      SmallPair(5, 10),
      SmallPair(49, 2)
    )
    obj2.validationErrors should equal (List(
      ValidationError(List("b"), "has a total greater than 50", SmallPair(49, 2))
    ))
  }

}
*/
