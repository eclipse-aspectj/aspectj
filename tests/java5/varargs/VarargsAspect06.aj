public aspect VarargsAspect06 {

  before(): call(* *(int,Integer...)) { }

}
