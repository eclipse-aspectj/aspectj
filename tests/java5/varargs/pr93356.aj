// "Varargs with .. in pointcut"

class A{
	public A(int i, int... nums) {}
	public void b(int i, int... nums) {}
}
aspect B{
	///////// methods	
	declare warning: execution(* *.*(..)) : "a";
	declare warning: execution(* *.*(int,..)) : "b";
	declare warning: execution(* *.*(..,int...)) : "c";
	declare warning: execution(* *.*(int,..,int...)) : "d";
	declare warning: execution(* *.*(int,int ...,..)) : "e";
	
	declare warning: execution(* *.*(..,int,..,int ...,..)) : "k";
	declare warning: execution(* *.*(..,..,*...,..,..)) : "l";
	
	
	declare warning: execution(* *.*(int,int [],..)) : "shouldn't match A";
	declare warning: execution(* *.*(int,int [])) : "shouldn't match B";
	
	//////////////////// constructors
	declare warning: execution(*.new(..)) : "f"; // matches  constructors for A and B
	declare warning: execution(*.new(int,..)) : "g";
	declare warning: execution(*.new(..,int...)) : "h";
	declare warning: execution(*.new(int,..,int...)) : "i";
	declare warning: execution(*.new(int,int ...,..)) : "j";
	
	declare warning: execution(*.new(int,int [],..)) : "shouldn't match C";
	declare warning: execution(*.new(int,int [])) : "shouldn't match D";
}





