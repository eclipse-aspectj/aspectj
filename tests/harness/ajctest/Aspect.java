
import org.aspectj.lang.*;
import org.aspectj.lang.reflect.*;

aspect Aspect {
    before() : execution(public * (!java..*).*(..)) {
        SourceLocation sl = thisJoinPointStaticPart.getSourceLocation();
        String s = thisJoinPoint + "@" + sl.getFileName() + ":" + sl.getLine();
        System.err.println(s);
    }
}
