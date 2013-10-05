package so.delicious.validation.macros

import scala.language.experimental.macros
import scala.language.implicitConversions
import scala.language.higherKinds

trait FieldExpressionDslBase {
  /**
   * An abstract wrapper type.
   *
   * If there is an implicit `FieldExpression[T] => W[T]` then we provide an implicit
   * `T => W[T]` for any field expression typed T.
   *
   * This enables "adding" arbitrary methods to arbitrary field expressions,
   * which would not be possible with only `FieldExpressionMacros.toFieldExpression`.
   */
  type W[_]

  implicit def toFieldExpressionWithWrapper[T](expr: T)(implicit wrap: FieldExpression[T] => W[T]): W[T] =
    macro FieldExpressionMacros.toFieldExpressionWithWrapper_impl[T, W]
}
