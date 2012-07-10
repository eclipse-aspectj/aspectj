package de.example;

import de.example.A.InnerA;

public aspect BAspect {

	InnerA B.someMethod(){
		A<String> as = new A<String>();
		return as.new InnerA();
	}

	InnerA B.someOtherMethod(){
		return someMethod();
	}

}
