package b_impl;

import a_impl.AImpl;
import b_intf.B;

public class BImpl
  extends AImpl
  implements B
{
  public B g()
  {
	System.out.println( "g called" );
	return null;
  }
  
  public static void main(String[] args) {
  	new BImpl().g();
  	new BImpl().f();
  }
}
