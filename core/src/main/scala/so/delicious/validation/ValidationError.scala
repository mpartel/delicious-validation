package so.delicious.validation

case class ValidationError(context: Seq[Symbol], message: String, value: Any) {
  def inContext(sym: Symbol) = this.copy(context = sym +: context)
}
