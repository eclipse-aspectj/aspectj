package com.test;

public class pr102933 {

    public void test() {
    	pr102933[] array = new pr102933[0];
    	pr102933[] arrayClone = (pr102933[])array.clone();
    }
}

aspect MyAspect {
    declare warning: call(* *(..)) :
	       "a call within pr102933";
}