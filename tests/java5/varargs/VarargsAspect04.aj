public aspect VarargsAspect04 {

  before(): withincode(* *(Integer[])) && call(* *(..)) { }

}
