package so.delicious.validation

case class ValidationError(context: Seq[String], message: String, value: Any)
