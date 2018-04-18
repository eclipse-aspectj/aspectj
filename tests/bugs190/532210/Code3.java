import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@interface PA {}

@Retention(RetentionPolicy.RUNTIME)
@interface TA {}

@TA class P {}

aspect X {
	declare warning: execution(* a(@PA (@TA *))): "has param anno and type anno";
}

public class Code3 {
	public static void main(String []argv) {
		Object o = (Object[])argv;
	}

	public void a(P p) {}
	public void b(String q) {}
	public void c(@PA P p) {}
	public void d(@PA String q) {}
}
