import org.aspectj.testing.Tester;

aspect ClientFlow percflow(entries(Client)) {
    pointcut entries(Client c):
	this(c) && (call(void Server.doService1(Object)) ||
                    call(void Server.doService2()));

    Client client;
    before (Client c): entries(c) { client = c; }

    pointcut workPoints():
	(this(ServiceWorker1) && call(void doWorkItemA())) ||
	(this(ServiceWorker2) && call(void doWorkItemB())) ||
	(this(ServiceWorker3) && call(void doWorkItemC())) ||
	(this(ServiceWorker4) && call(void doWorkItemD()));

    Object around(): workPoints() {
	//System.out.println("at work: " + thisJoinPoint.methodName);
	client.count++;
	return proceed();
	//return;
    }

    void util(Client c) {
        c.count++;
        client.count++;
    }
}


public class Client {
    public static void main(String[] args) { test(); }
    public static void test() {
	Client c = new Client();
	Server s = new Server();

	c.requestServices(s);

	Tester.checkEqual(c.count, 5, "A+B+C+2*D");

	Tester.check("WorkA");
	Tester.check("WorkB");
	Tester.check("WorkC");
	Tester.check("WorkD");
    }

    int count;

    public void requestServices(Server s) {
	s.doService1("foo");
	s.doService2();
    }
}

class Server {

    ServiceWorker1 worker1 = new ServiceWorker1();
    ServiceWorker2 worker2 = new ServiceWorker2();

    public void doService1(Object data) {
	worker1.doYourPart();
    }

    public void doService2() {
	worker2.doYourPart();
    }
}

class ServiceWorker1 {
    void doYourPart() {
	doWorkItemA();
    }

    void doWorkItemA() { Tester.note("WorkA");}
}

class ServiceWorker2 {
    ServiceWorker3 worker3 = new ServiceWorker3();
    ServiceWorker4 worker4 = new ServiceWorker4();

    void doYourPart() {
	worker3.doYourPart();
	worker4.doYourPart();
	doWorkItemB();
    }

    void doWorkItemB() { Tester.note("WorkB");}

}

class ServiceWorker3 {
    void doYourPart() {
	doWorkItemC();
    }

    void doWorkItemC() { Tester.note("WorkC"); }
}

class ServiceWorker4 {
    void doYourPart() {
	doWorkItemD();
    }

    void doWorkItemD() {
        // charge extra for 'd' "by hand"
        ClientFlow.aspectOf().client.count++;
	Tester.note("WorkD");
    }
}
