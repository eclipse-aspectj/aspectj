abstract aspect StaticAdviceOnAbstract {
     abstract pointcut i();

     pointcut j(): 
	 i()
	 && !this(StaticAdviceOnAbstract)
	 && call(new(..)) ;
}

aspect Concrete {
     // static advice indirectly on an abstract pointcut
     after() returning(Object o): StaticAdviceOnAbstract.j() {
         System.out.println("we have"+o);
     }

     // a simple case of directly on abstract pointcut
     after() returning(Object o): StaticAdviceOnAbstract.i() {
         System.out.println("we have"+o);
     }
}
