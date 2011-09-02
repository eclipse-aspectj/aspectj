import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.aspectj.lang.reflect.FieldSignature;

import com.sun.org.apache.bcel.internal.classfile.Field;

@Retention(RetentionPolicy.RUNTIME)
@interface Marker {
	String message();
}

public class AnnoBinding2 {
	public static void main(String[] argv) {
		runOne();
		runTwo();
		java.lang.reflect.Field[] fs = AnnoBinding2.class.getDeclaredFields();
		int count = 0;
		for (java.lang.reflect.Field f: fs) {
			if (f.getName().startsWith("ajc$anno")) {
				count++;
			}
		}
		System.out.println(count+" ajc$anno$NNN fields");
	}

	@Marker(message = "foo")
	static int field1;

	@Marker(message = "bar")
	static int field2;

	public static void runOne() {
		field1 = field1 * 2; // set and get jps
	}

	public static void runTwo() {
		field2 = field2 * 2; // set and get jps
	}
}

aspect X {
	pointcut pWoven(Marker l): withincode(* run*(..)) && get(@Marker * * ) && @annotation(l);

	before(Marker l): pWoven(l) {
	    System.out.println(thisJoinPointStaticPart+" "+l);
	}
}
