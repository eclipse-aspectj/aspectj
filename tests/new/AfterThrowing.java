import org.aspectj.testing.Tester;

import java.util.*;

public class AfterThrowing {
    public static void main(String[] args) { test(); }

    public static void test() {
        throwException(true);
        Tester.check("Throwable:throwException");
        Tester.check("Exception:throwException");

        throwRuntimeException(true);
        Tester.check("Throwable:throwRuntimeException");
        Tester.check("RuntimeException:throwRuntimeException");

        throwError(true);
        Tester.check("Throwable:throwError");

        throwMyException(true);
        Tester.check("Throwable:throwMyException");
        Tester.check("Exception:throwMyException");
        Tester.check("MyException:throwMyException");
    }

    static void throwNothing(boolean b) { }

    static void throwException(boolean b) throws Exception {
        if (b) throw new Exception();
        throwError(false);
    }

    static void throwRuntimeException(boolean b) {
        if (b) throw new RuntimeException();
    }

    static String throwError(boolean b) {
        int[] i = new int[10];
        // this line is to make sure ajc doesn't think it needs to worry about a 
        // CloneNotSupportedException when arrays are cloned
        i = (int[])i.clone();

        if (b) throw new Error();
        return "foo";
    }

    static Object throwMyException(boolean b) throws MyException {
        if (b) throw new MyException();
        return new Integer(10);
    }

    public static class MyException extends Exception { }
}


aspect A {
    pointcut throwerCut(): within(AfterThrowing) && execution(* *(boolean));


    after () throwing (Throwable t): throwerCut() { 
        Tester.note("Throwable:" + thisJoinPointStaticPart.getSignature().getName());
    }

    after () throwing (Exception t): throwerCut() { 
        Tester.note("Exception:" + thisJoinPoint.getSignature().getName());
    }

    after () throwing (RuntimeException t): throwerCut() { 
        Tester.note("RuntimeException:" + thisJoinPointStaticPart.getSignature().getName());
    }

    after () throwing (AfterThrowing.MyException t): throwerCut() { 
        Tester.note("MyException:" + thisJoinPoint.getSignature().getName());
    }

    pointcut catchThrowsFrom(): 
        within(AfterThrowing) && call(* AfterThrowing.*(boolean));


    declare soft: Throwable: catchThrowsFrom();

    Object around(): catchThrowsFrom() {
        try {
            return proceed();
        } catch (Throwable t) {
            //System.out.println("caught " + t);
            return null;
        }
    }
}


