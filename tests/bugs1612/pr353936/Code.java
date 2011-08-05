public class Code {
  public void m() {
 
  }
}

aspect X {
  before(): execution(* m(..)) && cflow(adviceexecution()) {}

  before(): execution(* *(..)) {
    int i = 4;
    log("foo",thisJoinPoint);
    log(Integer.valueOf(i),thisJoinPoint);
  }
  public void log(String s,Object o) {}
  public void log(int s,Object o) {}
}

