package a.b.d;
//import a.b.c.I;



public aspect NPE {
  
  pointcut ii(I i) : execution(* I.*(..)) && this(i);

  after(I i) returning : ii(i) {
      System.out.println(i);
  }
  
}
