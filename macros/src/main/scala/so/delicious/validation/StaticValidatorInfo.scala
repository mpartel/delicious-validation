package so.delicious.validation

import scala.annotation.implicitNotFound

class StaticValidatorInfo[T](
  val defaultSubvalidateds: Iterable[(List[String], AbstractValidated)]
)
