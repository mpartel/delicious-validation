package so.delicious.validation

case class FieldExpression[T](components: List[Symbol], value: T)
