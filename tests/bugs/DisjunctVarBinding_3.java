aspect IfPointcut { 

	after(A a, B b) returning:
	   call(* foo(*,*)) && 
	(args(b,a) || args(a,b)) { 
		System.out.println("Woven"); 
	}
}