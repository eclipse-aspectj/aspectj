public class Tricky1 {
    public static void main(String[] args) {
	Product1 p1, p2, p3;
	p1 = Product1.create();	
	p2 = Product1.create();
	p3 = Product1.create();
	System.out.println("p1: " + p1 + ", p2: " + p2 + ", p3: " + p3);
    }
}


class Product1 {
    public static Product1 create() {
	return new Product1();
    }
}

aspect MakeProduct1Singleton {
    private static Product1 singletonProduct1 = null;

    // all calls to the Product1.create() method
    pointcut creationCut(): calls(Product1, new(..)); //Product1 create());

    // all calls to the above that don't happen in this aspect
    pointcut externalCreationCut(): !within(MakeProduct1Singleton) && creationCut();

    static around () returns Product1: externalCreationCut() {
	if (singletonProduct1 == null) {
	    singletonProduct1 = new Product1(); //Product1.create();
	}
	return singletonProduct1;
    }
}
