import org.aspectj.testing.Tester;

public class AfterReturningHandler {
    public static void main(String[] args) {
        
        AfterReturningHandler p = new AfterReturningHandler();
        p.mIntThrowing();
        Tester.checkAndClearEvents(new String[] {"returned null" });  // will probably say "returned null"  for all below
        p.mVoidThrowing();
        Tester.checkAndClearEvents(new String[] {  });
        p.mIntThrowingReturning();
        Tester.checkAndClearEvents(new String[] {  });
        p.mVoidThrowingReturning();
        Tester.checkAndClearEvents(new String[] {  });


                                                  
    }

    public void mVoidThrowing() {
        try { throw new RuntimeException(); }
        catch (RuntimeException e) {
        }
    }

    public int mIntThrowing() {
        try { throw new RuntimeException(); }
        catch (RuntimeException e) {
        }
        return 3;
    }
    public void mVoidThrowingReturning() {
        try { throw new RuntimeException(); }
        catch (RuntimeException e) {
            return;
        }
    }

    public int mIntThrowingReturning() {
        try { if (true) throw new RuntimeException(); }
        catch (RuntimeException e) {
            return 3;            
        }
        return 999999;
    }

    AfterReturningHandler() {
    }
}


aspect A {
    private void callEvent(String s, Object o) {
        Tester.event(s + " " + o);
    }

    after() returning (Object o) : handler(RuntimeException) { 
        callEvent("returned ", o);
    }
}    
