package bugs;

public class PerTargetSubaspectError {
       public static void main(String[] args) {
               C.run();
       }
       static class C {
               static void run() {}
       }
       pointcut doit() : execution(void C.run());

       // no error if not pertarget
       static aspect CPT pertarget(pc()){
               // no error if doit() defined in CPT
               protected pointcut pc() : doit(); // unexpected CE
               before() : doit() {} // no CE
       }
}
