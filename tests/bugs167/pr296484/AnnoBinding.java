import org.aspectj.lang.*;
import org.aspectj.lang.reflect.*;
import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME) 
@interface Marker {
   String message();
}

public class AnnoBinding {
  public static void main(String []argv) {
    long stime = System.currentTimeMillis();
    for (int i=0;i<10000;i++) {
       runOne();
    }
    long etime = System.currentTimeMillis();
    long manual = (etime-stime);
    stime = System.currentTimeMillis();
    for (int i=0;i<10000;i++) {
       runTwo();
    }
    etime = System.currentTimeMillis();
    long woven = (etime-stime);
    System.out.println("woven="+woven+" manual="+manual);
    if (woven>manual) {
      throw new RuntimeException("woven="+woven+" manual="+manual);
    }
    if (X.a!=X.b) {
      throw new RuntimeException("a="+X.a+" b="+X.b);
    }
  }

  @Marker(message="string")
  public static void runOne() {
  }

  @Marker(message="string")
  public static void runTwo() {
  }
  
  static Annotation ajc$anno$1;
}

aspect X {
  
   pointcut pManual(): execution(@Marker * runOne(..));
   pointcut pWoven(Marker l): execution(@Marker * runTwo(..)) && @annotation(l);

   public static int a,b;

   before(): pManual() {
     Marker marker = (Marker) ((MethodSignature) thisJoinPointStaticPart.getSignature()).getMethod().getAnnotation(Marker.class);
     String s = marker.message();
     a+=s.length();
   }

   before(Marker l): pWoven(l) {
     String s = l.message();
     b+=s.length();
   }
}

//
//0:   invokestatic    #96; //Method X.aspectOf:()LX;
//3:   getstatic       #108; //Field ajc$anno$0:Ljava/lang/Annotation;
//6:   dup
//7:   ifnonnull       30
//10:  ldc     #1; //class AnnoBinding
//12:  ldc     #109; //String runTwo
//14:  iconst_0
//15:  anewarray       #111; //class java/lang/Class
//18:  invokevirtual   #115; //Method java/lang/Class.getDeclaredMethod:(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Me
//d;
//21:  ldc     #104; //class Marker
//23:  invokevirtual   #121; //Method java/lang/reflect/Method.getAnnotation:(Ljava/lang/Class;)Ljava/lang/annotation/Annotation;
//26:  dup
//27:  putstatic       #108; //Field ajc$anno$0:Ljava/lang/Annotation;
//30:  nop
//31:  checkcast       #104; //class Marker
//34:  invokevirtual   #125; //Method X.ajc$before$X$2$ea6844ce:(LMarker;)V
//37:  return