public aspect pr112027 {
  pointcut pc() : this(pr112027);
  before(pr112027 tis) : pc() && this(tis) { }
}
