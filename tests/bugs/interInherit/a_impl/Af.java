package a_impl;

import a_intf.A;

aspect Af
{
  public A AImpl.f()
  {
	System.out.println( "f called" );
	return null;
  }
}
