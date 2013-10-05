package so.delicious.validation

/** Thrown from `Validated#throwIfInvalid`. */
class ValidationException(val errors: Seq[ValidationError]) extends IllegalArgumentException("The object was invalid")
