public aspect VarargsAspect02 {

  @org.aspectj.lang.annotation.SuppressAjWarnings
  before(): execution(* *(Integer[])) { }

}
