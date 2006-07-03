package pkg;

public aspect A8 {
	
	pointcut p(Integer value) : set(Integer memory) && args(value); 
	
	after(Integer value) returning : p(value) {	
	}
	
}
