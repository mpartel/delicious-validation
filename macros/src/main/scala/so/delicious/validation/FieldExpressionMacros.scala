package so.delicious.validation

import scala.language.experimental.macros
import scala.language.implicitConversions
import scala.language.higherKinds
import scala.reflect.macros.Context

object FieldExpressionMacros {
  /**
   * DSL implementation detail.
   *
   * Converts an expression to a FieldExpression.
   */
  def toFieldExpressionMacro[T](c: Context)(expr: c.Expr[T]): c.Expr[FieldExpression[T]] = {
    import c.universe._

    def findComponents(t: c.Tree): List[String] = {
      t match {
        case This(_) => Nil
        case Select(left, right) => right.decoded :: findComponents(left)
        case _ => c.abort(t.pos,
          "Not a field selection expression. " +
          "A field selection expression may be `this` or refer to any field or subfield of `this`."
        )
      }
    }

    val components = findComponents(expr.tree).reverse
    val componentLiterals = components.map(c.literal(_).tree)
    val listCtor = reify { List }.tree
    val listMakingTree = Apply(Select(listCtor, newTermName("apply")), componentLiterals)
    val listMakingExpr = c.Expr[List[String]](listMakingTree)

    reify { FieldExpression(listMakingExpr.splice.map(Symbol(_)), expr.splice) }
  }

  /**
   * DSL implementation detail.
   *
   * Converts an expression to a wrapper type W, where W can be built from a field expression.
   */
  def toFieldExpressionWithWrapperMacro[T, W[_]](c: Context)(expr: c.Expr[T])(wrap: c.Expr[FieldExpression[T] => W[T]]): c.Expr[W[T]] = {
    import c.universe._

    val fieldExprMakingExpr = toFieldExpressionMacro[T](c)(expr)
    reify { wrap.splice.apply(fieldExprMakingExpr.splice) }
  }
}
