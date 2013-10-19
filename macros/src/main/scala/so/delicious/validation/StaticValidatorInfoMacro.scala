package so.delicious.validation

import scala.reflect.macros.Context

object StaticValidatorInfoMacro {
  def staticValidatorInfoMacro[T](c: Context)(implicit typeTagOfT: c.WeakTypeTag[T]): c.Expr[StaticValidatorInfo[T]] = {
    import c.universe._

    val macroPos = c.macroApplication.pos

    val abstractValidatedTy = c.typeOf[AbstractValidated]
    val optionAbstractValidatedTy = c.typeOf[Option[AbstractValidated]]
    val iterableAbstractValidatedTy = c.typeOf[Iterable[AbstractValidated]]
    val mapOfAbstractValidatedTy = c.typeOf[Map[_, AbstractValidated]]

    type Subvalidateds = Iterable[(List[String], AbstractValidated)]

    def subvalidatedsExpr(symbol: TermSymbol): Option[c.Expr[Iterable[(List[String], AbstractValidated)]]] = {
      val name = c.Expr[String](Literal(Constant(symbol.name.decoded)))
      val ty = symbol.typeSignature
      val fieldRef = treeBuild.mkAttributedThis(symbol)

      if (ty <:< abstractValidatedTy) {
        val fieldExpr = c.Expr[AbstractValidated](fieldRef)
        Some(reify { List(List(name.splice) -> fieldExpr.splice) })

      } else if (ty <:< optionAbstractValidatedTy) {
        val fieldExpr = c.Expr[Option[AbstractValidated]](fieldRef)
        Some(reify { fieldExpr.splice.view.map(List(name.splice) -> _).toList })

      } else if (ty <:< iterableAbstractValidatedTy) {
        val fieldExpr = c.Expr[Iterable[AbstractValidated]](fieldRef)
        Some(reify { fieldExpr.splice.view.zipWithIndex.map { case (v, i) => List(name.splice, i.toString) -> v } })

      } else if (ty <:< mapOfAbstractValidatedTy) {
        val fieldExpr = c.Expr[Map[_, AbstractValidated]](fieldRef)
        Some(reify { fieldExpr.splice.view.map { case (k, v) => List(name.splice, k.toString) -> v } })

      } else {
        None
      }
    }

    val classTy = typeTagOfT.tpe
    if (!classTy.typeSymbol.isClass) {
      c.abort(macroPos, s"The given type ${classTy} is not a (known) class")
    }
    if (!(classTy <:< abstractValidatedTy)) {
      c.abort(macroPos, s"The given type ${classTy} does not extend AbstractValidated")
    }

    val ctorSym = classTy.member(nme.CONSTRUCTOR).asMethod

    val subvalidatedsList: List[c.Expr[Subvalidateds]] = ctorSym.paramss match {
      case Nil => Nil
      case firstParamList :: _ => {
        val memberSyms = firstParamList.map(p => classTy.member(p.name))
        memberSyms.flatMap { ms =>
          if (ms.isTerm && ms.isPublic) subvalidatedsExpr(ms.asTerm)
          else None
        }
      }
    }

    val subvalidatedExpr = subvalidatedsList.foldRight(reify { Nil: Iterable[(List[String], AbstractValidated)] }) { case (left, right) =>
      reify { left.splice ++ right.splice }
    }

    reify { new StaticValidatorInfo(subvalidatedExpr.splice) }
  }
}
