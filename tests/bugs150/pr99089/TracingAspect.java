import java.util.Vector;

privileged aspect TracingAspect {
	before(DataClass dc): execution(* DataClass.doit()) && this(dc) {
		Vector<Object> myV = dc.getV();
	}
}
