import org.aspectj.lang.*;
import org.aspectj.testing.*;
import java.io.*;

public class EmptyStack{
    public static void main(String[] args){
        try {
            EmptyStackAspect.InnerEmptyStackAspect a = EmptyStackAspect.InnerEmptyStackAspect.aspectOf();
        } catch (java.util.EmptyStackException ese) {
            String msg = "";
            class PW extends PrintWriter {
                PW() { super((Writer)null); }
                String msg = "";
                public void write(int c) { msg += c; }
            }
            PW pw = new PW();
            ese.printStackTrace(pw);
            Tester.check(false, pw.msg);
        } catch (NoAspectBoundException nae) {
	    Tester.note("caught NoAspectBound");
	}

	Tester.check("caught NoAspectBound");
    }
}

abstract aspect EmptyStackAspect {
    pointcut testCut();

    public static aspect InnerEmptyStackAspect percflow(testCut()){
    }
}

aspect MyEmptyStackAspect extends EmptyStackAspect issingleton() {
    pointcut testCut(): call(void EmptyStack.test(..)) ;
}
