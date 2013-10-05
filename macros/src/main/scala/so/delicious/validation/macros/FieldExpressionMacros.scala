package so.delicious.validation.macros

import scala.language.experimental.macros
import scala.language.implicitConversions
import scala.language.higherKinds
import scala.reflect.macros.Context

trait FieldExpressionMacros {
  import FieldExpressionMacros._
  implicit def toFieldExpression[T](expr: T): FieldExpression[T] = macro toFieldExpression_impl[T]
}

object FieldExpressionMacros extends FieldExpressionMacros {
  def toFieldExpression_impl[T](c: Context)(expr: c.Expr[T]): c.Expr[FieldExpression[T]] = {
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

  def toFieldExpressionWithWrapper_impl[T, W[_]](c: Context)(expr: c.Expr[T])(wrap: c.Expr[FieldExpression[T] => W[T]]): c.Expr[W[T]] = {
    import c.universe._

    val fieldExprMakingExpr = toFieldExpression_impl[T](c)(expr)
    reify { wrap.splice.apply(fieldExprMakingExpr.splice) }
  }
}
