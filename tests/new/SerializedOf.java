import org.aspectj.testing.Tester;
import java.io.*;


public class SerializedOf {
    public static void main(String[] args) throws Exception {
	C c = new C();

	Tester.check(ASer.hasAspect(c), "ASer on original");
	Tester.check(ANoSer.hasAspect(c), "ANoSer on original");


	File tmp = File.createTempFile("cdata", "ser");
	FileOutputStream ostream = new FileOutputStream(tmp);
	ObjectOutputStream p = new ObjectOutputStream(ostream);
	p.writeObject(c);
	p.flush();
	ostream.close();


	FileInputStream istream = new FileInputStream(tmp);
	ObjectInputStream p1 = new ObjectInputStream(istream);
	C newC = (C)p1.readObject();
	istream.close();

	Tester.check(ASer.hasAspect(newC), "ASer after read");
	Tester.check(!ANoSer.hasAspect(newC), "no ANoSer after read");
    }
}

class C implements Serializable {
    int data = 42;
}


aspect ASer implements Serializable pertarget(target(C)) {
    int serData = 20;
}

aspect ANoSer pertarget(target(C+)) {
    int noSerData = 21;
}
