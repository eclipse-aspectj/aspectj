import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@interface AnnoShort {
  short value() default 3;
  short sss() default 3;
}

@Retention(RetentionPolicy.RUNTIME)
@interface AnnoDouble {
  double value() default 3.0d;
  double ddd() default 3.0d;
}

@Retention(RetentionPolicy.RUNTIME)
@interface AnnoFloat {
  float value() default 4.0f;
  float fff() default 4.0f;
}

@Retention(RetentionPolicy.RUNTIME)
@interface AnnoChar {
  char value() default 'a';
  char ccc() default 'a';
}

@Retention(RetentionPolicy.RUNTIME)
@interface AnnoByte {
  byte value() default 66;
  byte bbb() default 66;
}

@Retention(RetentionPolicy.RUNTIME)
@interface AnnoInt {
  int value() default 111;
  int iii() default 111;
}
