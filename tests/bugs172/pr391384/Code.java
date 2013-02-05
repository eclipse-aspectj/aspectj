import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@interface Anno {
  String[] value();
}

aspect Code {

  @Anno({"xyz","abc"})
  public void m() {}

  pointcut p(): execution(public * @Anno(value="xyz=abc") *..*(..));

  before() : p() { }

}
