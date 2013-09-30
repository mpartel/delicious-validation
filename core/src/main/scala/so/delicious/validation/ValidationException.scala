package so.delicious.validation

class ValidationException(val errors: Seq[ValidationError]) extends IllegalArgumentException("The object was invalid")
