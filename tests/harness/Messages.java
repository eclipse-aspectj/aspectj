
public class Messages {
    public static void main (String[] args) {
        new C().run();   
    } 
}

class C {
    void run() {
    }
}

aspect A {
    pointcut f() : receptions(void C.run()); // ME 14
    around() returns void : f() { }          // ME 15
}
