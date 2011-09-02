import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.aspectj.lang.reflect.FieldSignature;

@Retention(RetentionPolicy.RUNTIME)
@interface Marker {
	String message();
}

public class AnnoBinding {
	public static void main(String[] argv) {
		long stime = System.currentTimeMillis();
		for (int i = 0; i < 10000; i++) {
			runOne();
		}
		long etime = System.currentTimeMillis();
		long manual = (etime - stime);
		stime = System.currentTimeMillis();
		for (int i = 0; i < 10000; i++) {
			runTwo();
		}
		etime = System.currentTimeMillis();
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
