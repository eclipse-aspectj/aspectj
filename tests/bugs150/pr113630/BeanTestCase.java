package com.blueprint.util.aspectj5.test;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import junit.framework.TestCase;

public class BeanTestCase extends TestCase implements PropertyChangeListener{

	public void setUp() throws Exception{
		super.setUp();
	}

	public void TearDown() throws Exception{
		super.setUp();
	}
	
	public void propertyChange(PropertyChangeEvent e){
	      System.out.println("Property [" + e.getPropertyName() + "[ changed from " +
	    		  							e.getOldValue() + " to " + e.getNewValue() );
    }
	
    public static void main(String [] argv) {
      new BeanTestCase().testPropertyChange();
    }
	public void testPropertyChange(){
		Bean b = new Bean();
		b.addPropertyChangeListener( "name", this );
		b.setName( "Test" );
		assertEquals( b.getName() , "Test" );
		b.setName( "Test1" );
		assertEquals( b.getName() , "Test1" );
	}
}
