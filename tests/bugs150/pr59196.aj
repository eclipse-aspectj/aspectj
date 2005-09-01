aspect some_aspect {
   pointcut call_m(int a, int b) : call(int *.m(..)) && args(a, b);
 
   int m(int p, int q) { return 2; }
   
   void foo() {
	   m(1,4);
   }
   
   int around(int x, int y) : call_m(x, y) {  return 5; }
}
 
aspect other_aspect {
   before(int x, int y) : 
       adviceexecution() && within(some_aspect) &&  args(x, y) {
	   
   }
}