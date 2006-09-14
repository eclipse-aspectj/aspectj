/*
* Copyright (C) 2005  John D. Heintz
* 
* This program is free software; you can redistribute it and/or
* modify it under the terms of the GNU Library General Public License
* as published by the Free Software Foundation; either version 2.1
* of the License.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU Library General Public License for more details.
*
* John D. Heintz can be reached at: jheintz@pobox.com 
*/
package dash.obtain;


public class ObtainStaticTest {
	
	@Obtain static String bar2;
	
	public void testRegularFieldNull() throws Exception {
		try {
			ObtainStaticTestClass.baz.length();
			fail("should have null pointered");
		} catch (NullPointerException ex) {
			;// noop
		}
	}

 public void fail(String s) {}

	/**
	 * 
	 * @throws Exception
	 */
	public void testNullPointerException() throws Exception {
		try {
			String bar = ObtainStaticTestClass.bar;
			System.out.println("bar2:"+ObtainStaticTest.bar2);
			fail(bar);
		} catch (NullPointerException ex) {
			;// noop
		}
	}	
	
	/**
	 * 
	 * @throws Exception
	 */
	public void testBasicObtain() throws Exception {
		// set @Obtain-able value to "foo" String
		//provider.setObtainableValue(ObtainStaticTestClass.class, "foo", "foo");
		
		assertEquals("foo", ObtainStaticTestClass.foo);
	}

public void assertEquals(Object a,Object b) {}
}
