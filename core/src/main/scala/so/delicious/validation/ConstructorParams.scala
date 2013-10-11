package so.delicious.validation
import scala.reflect.ClassTag
import scala.reflect.runtime.{universe => ru}

object ConstructorParams {
  private[this] case class TaggedObj[T](obj: T, classTag: ClassTag[T])

  @volatile private[this] var cache = Map.empty[Class[_], TaggedObj[_] => Seq[(Symbol, Any)]]

  // Note: we need to synchronize our access to the reflection API due to
  // https://issues.scala-lang.org/browse/SI-6240
  // (see also http://docs.scala-lang.org/overviews/reflection/thread-safety.html )
  // Class tag context bounds should be safe from what I can tell.

  private[this] def makeFieldMirrorsGetter(cls: Class[_]): TaggedObj[_] => Seq[(Symbol, Any)] = ru.synchronized { // SI-6240
    val mirror = ru.runtimeMirror(getClass.getClassLoader)

    val memberSyms = mirror.classSymbol(cls).baseClasses.flatMap { baseClassSym =>
      val ty = baseClassSym.asClass.typeSignature

      val ctorSym = ty.member(ru.nme.CONSTRUCTOR)
      if (ctorSym.isMethod) {
        ctorSym.asMethod.paramss(0).flatMap { param =>
          val memberSym = ty.member(param.name)
          if (memberSym.isTerm && memberSym.isPublic) {
            Some(memberSym.asTerm)
          } else {
            None
          }
        }
      } else {
        Seq.empty
      }
    }

    {
      case TaggedObj(obj, classTag) => ru.synchronized { // SI-6240
        val objMirror = mirror.reflect(obj)(classTag)
        memberSyms.map(m => Symbol(m.name.decoded) -> objMirror.reflectField(m).get)
      }
    }
  }

  def apply[T : ClassTag](obj: T): Seq[(Symbol, Any)] = {
    val taggedObj = TaggedObj(obj, implicitly[ClassTag[T]])
    val cls = obj.getClass

    val f = cache.get(cls) match {
      case Some(f) => f
      case None => {
        val f = makeFieldMirrorsGetter(cls)
        // There is a race condition here, but it's acceptable for a cache
        cache = cache + (cls -> f)
        f
      }
    }

    f(taggedObj)
  }
}
