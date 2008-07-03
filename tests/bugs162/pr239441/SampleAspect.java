import java.lang.annotation.*;

public aspect SampleAspect {
  declare parents : hasmethod(@Something * *.*(..)) implements SampleInterface;

  private interface SampleInterface{
  }
}

@Retention(RetentionPolicy.RUNTIME)
@interface Something {}