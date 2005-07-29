public aspect GetAndSetPointcutMatching {
	
	// rule 1) you can't use generic or parameterized type patterns in the declaring type position
	pointcut tryGetGeneric() : get( * Generic<T>.*); // CE L 4
	pointcut trySetGeneric() : set(* Generic<T>.*); // CE L5
	pointcut tryGetParameterized() : get(* ISore<String>.*); // CE L6
	pointcut trySetParameterized() : set(* ISore<String>.*); // CE L7
	pointcut trySneakyGet() : get(* (Object || ISore<Number>).*); // CE L8
}


class Generic<T> {
	
	T foo = null;
	
	T getFoo() {
		return foo;
	}
	
}

interface ISore<E> {
	
	void iSee(E anE);
	
}

class UglyBuilding implements ISore<String> {
	
	public void iSee(String s) {}
	
}