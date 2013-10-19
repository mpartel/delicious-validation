package so.delicious.validation

trait Validator[T] {
  def validate(obj: T)(implicit di: ValidationDependencyInjector = ValidationDependencyInjector.empty): List[ValidationError]
}
