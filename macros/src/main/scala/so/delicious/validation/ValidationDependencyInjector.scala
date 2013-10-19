package so.delicious.validation

import scala.reflect.runtime.{universe => ru}

trait ValidationDependencyInjector {
  def get[T : ru.TypeTag]: Option[T]
}

object ValidationDependencyInjector {
  val empty = new ValidationDependencyInjector {
    def get[T : ru.TypeTag] = None
  }
}
