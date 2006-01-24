package edu.ucsd.aosd;

import java.io.PrintStream;

public class MyApplication
{
    // main
    public static void main(String[] args) {
        for (String arg : args) {
            System.out.println("got: " + arg);
//            System.out.printf("got: %s\n", arg);
        }
    }
}

aspect Printing {
    pointcut printlnCalls(PrintStream ps, String out):
        call(* PrintStream+.println(String)) && target(ps) && args(out);
    Object around(PrintStream ps, String out):
        printlnCalls(ps, out) && !adviceexecution() {
        return proceed(ps, out);
    }

//    pointcut printfCalls(PrintStream ps, String fmt, Object[] objs):
//        call(* PrintStream+.printf(String, Object...#####))
//        && target(ps) && args(fmt, objs);
}
