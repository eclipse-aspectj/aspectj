public aspect VarargsAspect01 {

  before(): call(* *(Integer[])) { }

}
