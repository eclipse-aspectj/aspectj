import org.aspectj.testing.Tester;

public class IntroOrder {
    public static void main(String[] args) {
	Persist.HasPersistor hp1 = new Class1();
	Persist.HasPersistor hp2 = new Class2();
        
	Persistor p = new Persistor();
	hp1.setPersistor(p);
	Tester.checkEqual(p, hp1.getPersistor(), "basic intro");
    }
}

class Class1 {}
class Class2 {}

aspect A1 {
    declare parents: Class1 implements Persist.HasPersistor;
}

abstract aspect Persist {
    interface HasPersistor {
	// introduction below specifies this interface
    }
    
    private Persistor HasPersistor.persistor;
    public void HasPersistor.setPersistor(Persistor p) { persistor = p; }
    public Persistor HasPersistor.getPersistor() { return persistor; }

    abstract pointcut readMethods();
    
    abstract pointcut writeMethods();
    
    //advices
}

aspect A2 extends Persist {
    declare parents: Class2 implements HasPersistor;
    // concretize pointcuts
    
    pointcut readMethods();
    pointcut writeMethods();
}

class Persistor {}
