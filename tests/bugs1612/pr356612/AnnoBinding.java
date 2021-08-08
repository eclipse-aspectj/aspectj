import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.aspectj.lang.reflect.FieldSignature;

@Retention(RetentionPolicy.RUNTIME)
@interface Marker {
	String message();
}

public class AnnoBinding {
	public static void main(String[] argv) {
		long stime = System.nanoTime();
		// 10,000 or 100,000 rounds are too quick, making the test flaky on rare occasions
		final int ROUNDS = 1000 * 1000;
		for (int i = 0; i < ROUNDS; i++) {
			runOne();
		}
		long etime = System.nanoTime();
		long manual = (etime - stime);
		stime = System.nanoTime();
		for (int i = 0; i < ROUNDS; i++) {
			runTwo();
		}
		etime = System.nanoTime();
		long woven = (etime - stime);
		System.out.println("woven=" + woven + " manual=" + manual);
		if (woven > manual) {
			throw new RuntimeException("woven=" + woven + " manual=" + manual);
		}
		if (X.a != X.b) {
			throw new RuntimeException("a=" + X.a + " b=" + X.b);
		}
	}

	@Marker(message = "string")
	static int field1;

	@Marker(message = "string")
	static int field2;

	public static void runOne() {
		field1 = field1 * 2; // set and get jps
	}

	public static void runTwo() {
		field1 = field1 * 2; // set and get jps
	}

}

aspect X {
	  pointcut pManual(): withincode(* runOne(..)) && get(@Marker * *);
	  pointcut pWoven(Marker l): withincode(* runTwo(..)) && get(@Marker * * ) && @annotation(l);

	   public static int a,b;

	   before(): pManual() {
	     Marker marker = (Marker) ((FieldSignature) thisJoinPointStaticPart.getSignature()).getField().getAnnotation(Marker.class);
	     String s = marker.message();
	     a+=s.length();
	   }

	   before(Marker l): pWoven(l) {
	     String s = l.message();
	     b+=s.length();
	   }


}
