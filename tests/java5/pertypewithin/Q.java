aspect Q pertypewithin(P) {

  int ctr = 0;

  after(): execution(* runA(..)) {
    ctr=ctr+1;
  }

  after(): execution(* main(..)) {
     System.err.println("Q reporting "+ctr);
  }
  
}
