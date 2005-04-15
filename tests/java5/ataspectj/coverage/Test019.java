// "@AfterReturning with wrong number of args"

import org.aspectj.lang.annotation.*;

aspect A{
  @AfterReturning(value="call(* *..*(..))",returning="f")
  public void itsAFoo(Object f, int x) {
  }        
}
