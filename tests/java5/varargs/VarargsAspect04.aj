public aspect VarargsAspect04 {

  @org.aspectj.lang.annotation.SuppressAjWarnings
  before(): withincode(* *(Integer[])) && call(* *(..)) { }

}
