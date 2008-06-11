package p1;

import org.aspectj.lang.annotation.SuppressAjWarnings;

@SuppressAjWarnings
public aspect Foobar {
  before(): execution(* *(..)) {}
}
