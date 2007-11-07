package c.d;;

public aspect DurationMethod extends AbstractDurationMethod {
  public pointcut methods(): within(a.b.*) &&  call (public * a..*(..));
}
