public aspect VarargsAspect01 {

  @org.aspectj.lang.annotation.SuppressAjWarnings
  before(): call(* *(Integer[])) { }

}
