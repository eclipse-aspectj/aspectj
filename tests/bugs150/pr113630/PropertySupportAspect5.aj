package com.blueprint.util.aspectj5.test;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.reflect.Field;
import org.aspectj.lang.Signature;

public aspect PropertySupportAspect5 {
		
			PropertyChangeSupport PropertySupport.support = new PropertyChangeSupport(this);

			public interface PropertySupport{
				  public void addPropertyChangeListener( PropertyChangeListener listener );
				  public void addPropertyChangeListener( String propertyName,
					                                     PropertyChangeListener listener );
				  public void removePropertyChangeListener( String propertyName,
					                                        PropertyChangeListener listener );
				  public void removePropertyChangeListener( PropertyChangeListener listener );
				  public void hasListeners( String propertyName );
				  public void firePropertyChange( Bean b,
							 					  String property,
							 					  String oldval,
							 					  String newval );
			}
			
			public void PropertySupport.addPropertyChangeListener(PropertyChangeListener listener){
			    support.addPropertyChangeListener(listener);
			}
			
			public void PropertySupport.addPropertyChangeListener( String propertyName,
			                                                       PropertyChangeListener listener){

				support.addPropertyChangeListener(propertyName, listener);
		    }
		    
			public void PropertySupport.removePropertyChangeListener( String propertyName,
				                                                      PropertyChangeListener listener) {
				support.removePropertyChangeListener(propertyName, listener);
		    }
			
			public void PropertySupport.removePropertyChangeListener(PropertyChangeListener listener) {
			    support.removePropertyChangeListener(listener);
			}
			
			public void PropertySupport.hasListeners(String propertyName) {
			    support.hasListeners(propertyName);
			}

			pointcut callSetter( Bean b ) 
		    : call( @propertyChanger * *(..) ) && target( b );
			
			void around( Bean b ) : callSetter( b )  {
			    String propertyName = getField( thisJoinPointStaticPart.getSignature() ).
			    																getName();
				System.out.println( "The property is [" + propertyName + "]" );
				String oldValue = b.getName();
			    proceed( b );
			    b.firePropertyChange( b, propertyName, oldValue, b.getName());
			}

		    private Field getField( Signature signature ){
		    	Field field = null;
				System.out.println( "Getting the field name of [" +signature.getName() + "]" );
		    	
		    	try{
		    		String methodName = signature.getName();
		    		field = signature.getDeclaringType().
									getDeclaredField( methodName.
													   substring( 3,
												                  methodName.length() ).
												                  			toLowerCase());
		    		field.setAccessible(true);
		    	}catch( NoSuchFieldException nsfe ){
		    		nsfe.printStackTrace();
		    	}
				return field;
			}

			public void PropertySupport.firePropertyChange( Bean b,
									 						String property,
									 						String oldval,
									 						String newval) {
				System.out.println( "The property is [" + property + "]");
				System.out.println( "The old value is [" + oldval + "]");
				System.out.println( "The new value is [" + newval + "]");
				b.support.firePropertyChange( property,
							( oldval == null ) ? oldval : new String(oldval),
				            new String(newval));
			}
}