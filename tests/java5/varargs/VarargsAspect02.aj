public aspect VarargsAspect02 {

  before(): execution(* *(Integer[])) { }

}
