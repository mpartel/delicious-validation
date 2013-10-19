package so.delicious.validation

case class ValidationError(context: Seq[String], message: String, value: Any) {
  def inContext(ctx: String) = this.copy(context = ctx +: context)
  def inContext(ctx: List[String]) = this.copy(context = ctx ++ context)
}
