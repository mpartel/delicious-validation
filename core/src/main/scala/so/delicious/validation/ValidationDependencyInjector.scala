package so.delicious.validation

import scala.reflect.runtime.{universe => runtimeUniverse}

/**
 * Interface to a runtime dependency injector for validated objects.
 *
 * If you want to use `injected[Foo]` in your validatables then
 * you need to provide an implicit instance of this in the scope.
 * Please see the readme for further advice.
 */
trait ValidationDependencyInjector {
  /**
   * Returns an instance of `T` or throws an exception.
   *
   * It is suggested, but in no way required, for the thrown exception
   * to be `ValidationDependencyInjector.MissingDependencyException`.
   */
  def get[T : runtimeUniverse.TypeTag]: T
}

/**
 * Provides an empty injector, which always throws an exception, and a thread-local injector that
 * forwards to a thread-local value.
 */
object ValidationDependencyInjector {
  class MissingDependencyException(msg: String, cause: Throwable = null) extends RuntimeException(msg, cause)

  /**
   * An empty injector that always throws.
   */
  val empty: ValidationDependencyInjector = new ValidationDependencyInjector {
    def get[T : runtimeUniverse.TypeTag]: T = throw new MissingDependencyException(
      "The empty dependency injector was used."
    )
  }

  /**
   * Wraps a thread-local injector.
   *
   * Please see the readme for instructions.
   */
  object threadLocal extends ValidationDependencyInjector {
    private val tl = new InheritableThreadLocal[ValidationDependencyInjector]

    def setThreadLocal(value: ValidationDependencyInjector) {
      tl.set(value)
    }

    def clearThreadLocal() {
      tl.remove()
    }

    def getThreadLocal: ValidationDependencyInjector = tl.get

    def withThreadLocal[T](value: ValidationDependencyInjector)(block: => T): T = {
      val prev = getThreadLocal
      setThreadLocal(value)
      try {
        block
      } finally {
        setThreadLocal(value)
      }
    }

    def get[T : runtimeUniverse.TypeTag]: T = Option(tl.get).map(_.get[T]).getOrElse(
      throw new MissingDependencyException("Thread-local ValidationDependencyInjector was used but none was set for this thread.")
    )
  }

  /**
   * Contains an implicit definition of the thread-local ValidationDependencyInjector,
   * for convenient mixin or import.
   */
  trait ImplicitThreadLocal {
    implicit def implicitThreadLocalValidationDependencyInjector = threadLocal
  }

  object ImplicitThreadLocal extends ImplicitThreadLocal
}
