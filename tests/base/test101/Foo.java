
aspect Foo pertarget(target(Pos)) {
  int count = 0;

  before (): ( call(* getX(..)) || 
               call(* getY(..)) ||
               call(* move(..))  ) {
      count++;
  }
}     

