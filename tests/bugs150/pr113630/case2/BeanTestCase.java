
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class BeanTestCase implements PropertyChangeListener{

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
    if (!b.getName().equals("Test")) throw new RuntimeException("");
    b.setName( "Test1" );
    if (!b.getName().equals("Test1")) throw new RuntimeException("");
  }
}
