package concrete;
import java.util.Vector;

public abstract class SuperClass<T extends Cement> {
   Vector<T> someTs = new Vector<T>();
   public abstract Vector<T> getSomeTs();
   public abstract void addSomeTs(Vector<T> newTs);  
}
