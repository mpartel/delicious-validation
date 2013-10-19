package so.delicious.validation

object experiment {
  class Dep

  case class Foo(x: Int) extends Validated[Foo] {
    def validator = new ValidatorDsl[Foo] {
      // We could basically get rid of new ValidatorDsl,
      // but we'd still need some sort of extra thing that
      // implicitly or explicitly invokes the subvalidatable finding macro on our class.

      x ~ "is bad" when (x > 123)
    }
  }
}
