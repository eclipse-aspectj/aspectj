package test.ataspectj.pointcutlibrary;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

//@Aspect
public class PointcutLibrary {
//pointcut mainMethod(): execution(public void main(String[]));
@Pointcut("execution(public void main(String[]))") public void mainMethod () { }

}
