package so.delicious.validation.macros

import scala.language.experimental.macros
import scala.language.implicitConversions
import scala.reflect.macros.Context

case class FieldExpression[T](components: Seq[String], value: T) {
  def ~(msg: String) = FieldExpressionAndMsg(this, msg)
}

case class FieldExpressionAndMsg[T](expr: FieldExpression[T], msg: String)

trait FieldExpressionImplicits {
  implicit def toFieldExpression[T](expr: T): FieldExpression[T] = macro FieldExpressionImplicits.toFieldExpression_impl[T]
}

object FieldExpressionImplicits extends FieldExpressionImplicits {
  def toFieldExpression_impl[T](c: Context)(expr: c.Expr[T]): c.Expr[FieldExpression[T]] = {
    import c.universe._

    def findComponents(t: c.Tree): List[String] = {
      t match {
        case This(_) => Nil
        case Select(left, right) => right.decoded :: findComponents(left)
        case _ => c.abort(t.pos, "Not a field selection expression (i.e. `foo.bar.baz`)")
      }
    }

    val components = findComponents(expr.tree).reverse
    val listCtor = reify { List }.tree
    val componentLiterals = components.map(c.literal(_).tree)
    val listMakingTree = Apply(Select(listCtor, newTermName("apply")), componentLiterals)
    val listMakingExpr = c.Expr[List[String]](listMakingTree)

    reify { FieldExpression(listMakingExpr.splice, expr.splice) }
  }
}
