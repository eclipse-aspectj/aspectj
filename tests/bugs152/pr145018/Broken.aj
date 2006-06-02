import java.util.*;
import org.aspectj.lang.annotation.*;

@Aspect
public class Broken {

    @Pointcut("call( * *.someMethod(..)) && args(arg1, arg2, arg3, status) ")
    public void someMethod2( int arg1, int arg2, int arg3, Status status ) {}

    @Pointcut("someMethod2(arg1, arg2, arg3, status) && if()")
    public static boolean someMethod2if(int arg1, int arg2, int arg3, Status status) {
        return status.equals( Status.DELETED );
    }

    @Pointcut("cflow(execution( * *.doProcess(..) ) && args( context, *, args)) && this(SomeClass+) ")
    public void inSomeClass2(Context context, Map args) {}

    @After( "inSomeClass2(context,args) && someMethod2if(arg1, arg2, arg3, status) ")
    public void deleteManagerInSomeClass2( Context context, Map args, int arg1, int arg2, int arg3, Status status) {
//             _log.write( DEBUG, "STATUS2: " + status );
    }
}

class Status {
  public final static int DELETED = 1;
}

class Context {}

class SomeClass {

  public void doProcess(Context c,Object o, Map m) {
 
    someMethod(1,2,3,new Status());
    someMethod(1,2,3,new Status());
    someMethod(1,2,3,new Status());
  }

  public void someMethod(int a, int b, int c, Status s) {
    doProcess(null,null,null);
  }
}
