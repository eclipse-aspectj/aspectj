// from Bug#:  28703  

class Base {
     /** extend when overriding - must call Base.lockResource() */
      public void lockResource(boolean dummy) { /* ... */ }
}

class Derived extends Base {
      boolean isLocked;

      public void lockResource(boolean callSuper) {
      	  if (callSuper) super.lockResource(true);
          isLocked = true;
      }
}
 
public aspect EnsureOverriding pertarget(mustExtend()) {
    boolean calledSuper = false;
    pointcut mustExtend() : 
        execution(void Base+.lockResource(..)) && !within(Base);
 
    after () returning: mustExtend() {
        assert(calledSuper);
        if (!calledSuper) { throw new RuntimeException("need super call"); }
    }
 
    after(Base a, Base b) returning: 
            cflow(mustExtend() && target(a)) && execution(void Base.lockResource(..)) && target(b) 
    {
        if (a == b) {
                //System.err.println("made call");
            calledSuper = true;
        }
    }
 
     public static void main(String args[]) {
         (new Derived()).lockResource(true);
         try {
         	(new Derived()).lockResource(false);
         	throw new Error("shouldn't get here");
         } catch (RuntimeException re) {
         	if (!re.getMessage().equals("need super call")) throw re;
         }	
     }
}
