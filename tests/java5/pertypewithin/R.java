aspect R pertypewithin(P) {

  int ctr = 0;

  after(): execution(* runB(..)) {
    ctr=ctr+1;
  }

  after(): execution(* main(..)) {
     System.err.println("R reporting "+ctr);
  }
  
}
