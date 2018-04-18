import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@interface Nullable {}

interface I {
	void n(Object o);
}

public class Code implements I {
	public void m(@Nullable Object v) {}
	@Override public void n(@Nullable Object v) {}
}

aspect X {
	 before(Object o): execution(* *(!@Nullable (*))) && args(o) { }
}
