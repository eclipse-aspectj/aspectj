import org.aspectj.testing.*;

public class PR335 {
    public static void main(String[] args) {
        Ship s = new Ship();
        Tester.clearEvents();
        s.handleCollision();
        Tester.checkAndClearEvents("before3(d=0) inflictDamage(0) handleCollision()");
        s.handleCollision("dummy");
        Tester.checkAndClearEvents("before1(so=dummy,d=1) inflictDamage(1) handleCollision(Object)");
        s.handleCollision(1);
        Tester.checkAndClearEvents("before2(so=1,d=2) inflictDamage(2) handleCollision(int)");
        s.inflictDamage(3);
        Tester.checkAndClearEvents("inflictDamage(3)");
    }
}


aspect Bug1 {
    pointcut collisionDamage(Ship s, Object so, int d) :
	this(s) 
        && cflow(call(void handleCollision(Object)) && args(so))
        && call(void Ship.inflictDamage(int)) && args(d);
    before(Ship s, Object so, int d) : collisionDamage(s, so, d) {
        Tester.event("before1(so="+so.toString()+",d="+d+")");
    }

    pointcut collisionDamage2(Ship s, int so, int d) :
	this(s) 
        && cflow(call(void handleCollision(int)) && args(so))
        && call(void Ship.inflictDamage(int)) && args(d);
    before(Ship s, Object so, int d) : collisionDamage2(s, so, d) {
        Tester.event("before2(so="+so+",d="+d+")");
    }

    pointcut collisionDamage3(Ship s, int d) :
	this(s)
        && withincode(void handleCollision())
        && call(void Ship.inflictDamage(int)) && args(d);
    before(Ship s, int d) : collisionDamage3(s, d) {
        Tester.event("before3(d="+d+")");
    }  
}

class Ship {
    void handleCollision() { inflictDamage(0); Tester.event("handleCollision()"); }
    void handleCollision(Object so) { inflictDamage(1); Tester.event("handleCollision(Object)"); }
    void handleCollision(int so) { inflictDamage(2); Tester.event("handleCollision(int)"); }
    void inflictDamage(int i) { Tester.event("inflictDamage(" + i + ")"); }
}
