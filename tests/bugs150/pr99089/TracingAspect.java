import java.util.Vector;

privileged aspect TracingAspect {
  before(DataClass dc): execution(* DataClass.doit()) && this(dc) {
    Vector<Object> myV = dc.getV();
    System.err.println("before:Length of v="+myV.size());
  }
  after(DataClass dc): execution(* DataClass.doit()) && this(dc) {
    Vector<Object> myV = dc.getV();
    System.err.println("after:Length of v="+myV.size());
  }
}
