

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class PersistabilityTest {

  public static void main(String []argv) throws Exception {

        PersistabilityTest persistabilityTest1 = new PersistabilityTest();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);

        oos.writeObject(persistabilityTest1);

        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bis);

        PersistabilityTest persistabilityTest2 = (PersistabilityTest) ois.readObject();

        if (!(persistabilityTest1 instanceof IPersistable)) throw new RuntimeException("pTest1 not IPersistable");
        if (!(persistabilityTest2 instanceof IPersistable)) throw new RuntimeException("pTest2 not IPersistable");
        int o = ((IPersistable)persistabilityTest1).getId();
        int o2 = ((IPersistable)persistabilityTest2).getId();
        if (o!=o2) throw new RuntimeException(o+" != "+o2);
    }
}
