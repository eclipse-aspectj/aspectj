
package test;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

aspect BoundEntityAspect {

        interface BoundBean extends Serializable {}

        declare parents: Model || *ChangeL implements BoundBean;//test.* && !BoundEntityAspect implements BoundBean;

        public synchronized PropertyChangeSupport BoundBean.getPropertyChangeSupport() {
        	return null;
        }

}

public class Model {

        private PropertyChangeListener changeListener = new ChangeL();

        protected class ChangeL implements PropertyChangeListener {
                public void propertyChange(PropertyChangeEvent evt) {
                        getPropertyChangeSupport();//.firePropertyChange("valid", null, null);
                }
        }

        public static void main(String[] args) {
                new Model().changeListener.propertyChange(null);
        }
}
