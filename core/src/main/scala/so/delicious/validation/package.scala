package so.delicious

import scala.language.experimental.macros
import scala.language.implicitConversions

package object validation {
  implicit def implicitStaticValidatorInfo[T <: AbstractValidated] = macro StaticValidatorInfoMacro.staticValidatorInfoMacro[T]
}
