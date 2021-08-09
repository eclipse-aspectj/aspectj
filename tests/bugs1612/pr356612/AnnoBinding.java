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
		long reflective = (etime - stime);
		stime = System.nanoTime();
		for (int i = 0; i < ROUNDS; i++) {
			runTwo();
		}
		etime = System.nanoTime();
		long bound = (etime - stime);
		String result = String.format("bound = %,d, reflective = %,d", bound, reflective);
		System.out.println(result);
		if (bound > reflective) {
			throw new RuntimeException(
				"Accessing annotation via bound parameter should be faster than reflective access: " + result
			);
		}
		if (X.sumReflective != X.sumBound) {
			throw new RuntimeException(
				String.format("Sums of @Marker message lengths should be equal: reflective = %,d, bound = %,d", X.sumReflective, X.sumBound)
			);
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
	pointcut pReflective(): withincode(* runOne(..)) && get(@Marker * *);
	pointcut pBound(Marker marker): withincode(* runTwo(..)) && get(@Marker * * ) && @annotation(marker);

	public static int sumReflective, sumBound;

	before(): pReflective() {
		Marker marker = (Marker) ((FieldSignature) thisJoinPointStaticPart.getSignature()).getField().getAnnotation(Marker.class);
		sumReflective += marker.message().length();
	}

	before(Marker marker): pBound(marker) {
		sumBound += marker.message().length();
	}

}
