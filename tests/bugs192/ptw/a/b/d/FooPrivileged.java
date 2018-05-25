package a.b.d;

privileged aspect Foo pertypewithin(Runnable+) {
	after() : staticinitialization(*) {
		  System.out.println("getWithinTypeName() = " + getWithinTypeName());
		  Class c = thisJoinPointStaticPart.getSourceLocation().getWithinType();
		  System.out.println("Aspect instance = "+Foo.aspectOf(c).getClass().getName());
	}
}
