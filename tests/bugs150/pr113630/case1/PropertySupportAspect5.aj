
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.reflect.Field;
import org.aspectj.lang.Signature;

public aspect PropertySupportAspect5 {
  
  declare parents: @javaBean * implements PropertySupport;
	
  public interface PropertySupport{  }
			
  public void PropertySupport.addPropertyChangeListener(PropertyChangeListener listener){  }
			
  public void PropertySupport.addPropertyChangeListener( String propertyName,PropertyChangeListener listener){  }
		    
  public void PropertySupport.removePropertyChangeListener( String propertyName, PropertyChangeListener listener) {  }
			
  public void PropertySupport.removePropertyChangeListener(PropertyChangeListener listener) {  }
			
  public void PropertySupport.hasListeners(String propertyName) {  }

  public void PropertySupport.firePropertyChange( Bean b, String property, String oldval, String newval) {  }
}
