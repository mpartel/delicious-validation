package so.delicious.validation

import org.scalatest.FreeSpec
import org.scalatest.Matchers
import org.scalatest.BeforeAndAfterEach
import scala.reflect.runtime.{universe => runtimeUniverse}

class ThreadLocalDependencyInjectionTest extends FreeSpec with Matchers with BeforeAndAfterEach {
  override def beforeEach() {
    ValidationDependencyInjector.threadLocal.clearThreadLocal()
  }

  override def afterEach() {
    ValidationDependencyInjector.threadLocal.clearThreadLocal()
  }

  class Dep1
  class Dep2

  class InjectorOfDep1Only extends ValidationDependencyInjector {
    def get[T : runtimeUniverse.TypeTag]: T = {
      import runtimeUniverse._
      if (typeOf[T] =:= typeOf[Dep1]) {
        (new Dep1).asInstanceOf[T]
      } else {
        throw new ValidationDependencyInjector.MissingDependencyException(typeOf[T].toString)
      }
    }
  }

  "is empty by default" in {
    import so.delicious.validation.ValidationDependencyInjector.ImplicitThreadLocal._

    new Validated {
      evaluating {
        injected[Dep1]
      } should produce [ValidationDependencyInjector.MissingDependencyException]
    }
  }

  "invokes the given injector" in {
    import so.delicious.validation.ValidationDependencyInjector.ImplicitThreadLocal._

    so.delicious.validation.ValidationDependencyInjector.threadLocal.withThreadLocal(new InjectorOfDep1Only) {
      new Validated {
        injected[Dep1] shouldBe a [Dep1]
        evaluating {
          injected[Dep2]
        } should produce [ValidationDependencyInjector.MissingDependencyException]
      }
    }
  }
}
