public aspect Aspect {
	
	after () returning : Pointcuts.main () && within(HelloWorld) {
		System.out.println(thisJoinPoint);
	}
}
