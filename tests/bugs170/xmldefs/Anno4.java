import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@interface AnnoString {
  String value() default "xyz";
  String sss() default "xyz";
}

@Retention(RetentionPolicy.RUNTIME)
@interface AnnoLong {
  long value() default 111L;
  long jjj() default 111L;
}

@Retention(RetentionPolicy.RUNTIME)
@interface AnnoBoolean {
  boolean value() default false;
  boolean zzz() default false;
}

@Retention(RetentionPolicy.RUNTIME)
@interface AnnoClass {
  Class value() default String.class;
  Class ccc() default String.class;
}
