public aspect VarargsAspect03 {

  before(): initialization(new(Integer[])) { }

}
