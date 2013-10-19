package so.delicious.validation

import scala.language.experimental.macros

private[validation] trait AbstractValidated {
  def validationErrors: Seq[ValidationError]
}
