import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;


public class A {
  public static void main(String[] args) throws Exception {
    A obj1 = new A();

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ObjectOutputStream oos = new ObjectOutputStream(baos);
    oos.writeObject(obj1);
    oos.close();
    byte[] data = baos.toByteArray();
    
    ByteArrayInputStream bais = new ByteArrayInputStream(data);
    ObjectInputStream ois = new ObjectInputStream(bais);
    Object o = ois.readObject();
    
    int before = ((Persistable)obj1).getPersistableId();
    int after = ((Persistable)o).getPersistableId();
    if (before!=after) 
    	System.out.println("The data was lost! before="+before+" after="+after);
    else
    	System.out.println("It worked, data preserved!");
  }
}

interface Persistable extends Serializable {
  abstract public int getPersistableId();
}

aspect PersistableImpl {
  declare parents: A extends Persistable;

  final public int Persistable.persistableId = 42;
  public int Persistable.getPersistableId() { return persistableId; }
}
