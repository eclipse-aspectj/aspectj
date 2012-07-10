package de.example;

import de.example.A.InnerA;

public aspect BAspect {

	InnerA B.someMethod(){
		return new InnerA();
	}

	InnerA B.someOtherMethod(){
		return someMethod();
	}

}
