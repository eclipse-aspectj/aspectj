abstract aspect GenericInheritedMethod<T> {

   protected  T getSomething() {
       return null;
   }

}


aspect pr124999 extends GenericInheritedMethod<Integer> {

   // Runtime Error
   void around() : execution(void someMethod()) {
       System.out.println(getSomething());
   }

   public static void main(String[] args) {
     new C().someMethod();
   }
   

}


class C {
   public void someMethod() { }
}
