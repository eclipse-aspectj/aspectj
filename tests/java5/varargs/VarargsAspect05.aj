public aspect VarargsAspect05 {

  before(): call(* *(Integer...)) { }

}
