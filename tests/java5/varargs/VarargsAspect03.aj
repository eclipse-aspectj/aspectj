public aspect VarargsAspect03 {

  @org.aspectj.lang.annotation.SuppressAjWarnings
  before(): initialization(new(Integer[])) { }

}
