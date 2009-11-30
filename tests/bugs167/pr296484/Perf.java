import org.aspectj.lang.*;
import org.aspectj.lang.reflect.*;
import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME) 
@interface Marker {
   String message();
}

public class Perf {
	
  public static void main(String []argv) {
	run(true);
	run(false);
  }
  
  public static void run(boolean warmup) {
    long stime = System.currentTimeMillis();
    for (int i=0;i<1000000;i++) {
       runOne();
    }
    long etime = System.currentTimeMillis();
    
    long manual = (etime-stime);
    stime = System.currentTimeMillis();
    for (int i=0;i<1000000;i++) {
       runTwo();
    }
    etime = System.currentTimeMillis();
    long woven = (etime-stime);

    stime = System.currentTimeMillis();
    for (int i=0;i<1000000;i++) {
       runThree();
    }
    etime = System.currentTimeMillis();
    long optimal = (etime-stime);

    if (!warmup) {
    	System.out.println("Manually fetching annotation with getAnnotation(): "+manual+"ms");
    	System.out.println("Binding annotation with @annotation(Marker): "+woven+"ms");
    	System.out.println("Binding annotation value with @annotation(Marker(message)): "+optimal+"ms");
    }
    if (woven>manual) {
    	throw new RuntimeException("woven = "+woven+" manual = "+manual);
    }
    if (optimal>woven) {
    	throw new RuntimeException("optimal = "+optimal+" woven = "+woven);
    }
  }

  @Marker(message="string")
  public static void runOne() {
  }

  @Marker(message="string")
  public static void runTwo() {
  }

  @Marker(message="string")
  public static void runThree() {
  }
}

aspect X {
   public static int a,b,c;
  
   // CaseOne: annotation fetching is done in the advice:
   pointcut adviceRetrievesAnnotation(): execution(@Marker * runOne(..));
   before(): adviceRetrievesAnnotation() {
     Marker marker = (Marker) ((MethodSignature) thisJoinPointStaticPart.getSignature()).getMethod().getAnnotation(Marker.class);
     String s = marker.message();
     a+=s.length();
   }

   // CaseTwo: annotation binding is done in the pointcut, advice retrieves message
   pointcut pointcutBindsAnnotation(Marker l): execution(@Marker * runTwo(..)) && @annotation(l);
   before(Marker l): pointcutBindsAnnotation(l) {
     String s = l.message();
     b+=s.length();
   }

   // CaseThree: annotation binding directly targets the message value in the annotation
   pointcut pointcutBindsAnnotationValue(String msg): execution(@Marker * runThree(..)) && @annotation(Marker(msg));
   before(String s): pointcutBindsAnnotationValue(s) {
     c+=s.length();
   }
}