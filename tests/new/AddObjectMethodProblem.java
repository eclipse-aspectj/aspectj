import org.aspectj.testing.Tester;

public class AddObjectMethodProblem {
    
    // Uncommenting this and changing addObject -> doAddObject in the advice
    // works around the bug 0.5beta1
    //public void doAddObject(Class1 o) {
	//    addObject(o);
    //}
        
    public static void main(String[] args) { test(); }
        
    crosscut ccut(Class1 c1): c1 && void process();

    advice(Class1 c1): ccut(c1) {
	    before {	    
            if (c1.getElement() != null) addObject(c1.getElement());
            c1.setProcessedAndAdvised(true);
	    }   
    }

    public static void test() {
        AddObjectMethodProblem a = new AddObjectMethodProblem();
	    Class1 c1 = new Class1();
        Class1 c2 = new Class1();
        c1.element = c2;
	    a.addObject(c1);
	    c1.process();
        Tester.check(c1.processedAndAdvised, "advice on top");
        Tester.check(c2.processedAndAdvised, "advice on element");
    }
}
	
class Class1 {
    public Class1 element;
    public boolean processedAndAdvised = false;
    
    public Class1 getElement() { return element; }
    
    public void setProcessedAndAdvised( boolean val ) { 
      processedAndAdvised = val;
    }
    
    public void process() {
        if (element != null) element.process();
    }
}

