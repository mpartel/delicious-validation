package so.delicious.validation.macros

case class FieldExpression[T](components: List[Symbol], value: T)
