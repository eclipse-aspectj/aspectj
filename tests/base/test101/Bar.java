
aspect Bar {
  static int count = 0;
  
   before (): target(Pos) && 
                    ( call(* getX(..)) || 
                      call(* getY(..)) ||
                      call(* move(..))  ) {
      count++;
  }
}
