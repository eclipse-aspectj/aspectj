import org.aspectj.testing.*;

public class StaticMembers_PR421 {
    public static void main(String[] args) {
        new StaticMembers_PR421().realMain(args);
    }
    public void realMain(String[] args) {

        // --- Statics
        Tester.checkEqual(Statics.I, new Integer(1+0));
        Tester.checkEqual(Statics.b, (byte) 1+0 );
        Tester.checkEqual(Statics.s, (short) 1+0 );
        Tester.checkEqual(Statics.i,  1+0 );
        Tester.checkEqual(Statics.l,  1+0L);
        Tester.checkEqual(Statics.f, (float) 1+0 );
        Tester.checkEqual(Statics.d, (double) 1+0 );
        Tester.checkEqual(Statics.c, '1'+0);

        // Statics.NestedStaticClass
        Tester.checkEqual(Statics.NestedStaticClass.I, new Integer(2+0));
        Tester.checkEqual(Statics.NestedStaticClass.b, (byte) 2+0 );
        Tester.checkEqual(Statics.NestedStaticClass.s, (short) 2+0 );
        Tester.checkEqual(Statics.NestedStaticClass.i,  2+0 );
        Tester.checkEqual(Statics.NestedStaticClass.l,  2+0L);
        Tester.checkEqual(Statics.NestedStaticClass.f, (float) 2+0 );
        Tester.checkEqual(Statics.NestedStaticClass.d, (double)(double) 2+0 );
        Tester.checkEqual(Statics.NestedStaticClass.c, '2'+0);

        Tester.checkEqual(Statics.NestedStaticClass.InnerStaticClass.I, new Integer(3+0));
        Tester.checkEqual(Statics.NestedStaticClass.InnerStaticClass.b, (byte) 3+0 );
        Tester.checkEqual(Statics.NestedStaticClass.InnerStaticClass.s, (short) 3+0 );
        Tester.checkEqual(Statics.NestedStaticClass.InnerStaticClass.i,  3+0 );
        Tester.checkEqual(Statics.NestedStaticClass.InnerStaticClass.l,  3+0L);
        Tester.checkEqual(Statics.NestedStaticClass.InnerStaticClass.f, (float) 3+0 );
        Tester.checkEqual(Statics.NestedStaticClass.InnerStaticClass.d, (double) 3+0 );
        Tester.checkEqual(Statics.NestedStaticClass.InnerStaticClass.c, '3'+0);

        Tester.checkEqual(Statics.NestedStaticClass.InnerStaticInterface.I, new Integer(4+0));
        Tester.checkEqual(Statics.NestedStaticClass.InnerStaticInterface.b, (byte) 4+0 );
        Tester.checkEqual(Statics.NestedStaticClass.InnerStaticInterface.s, (short) 4+0 );
        Tester.checkEqual(Statics.NestedStaticClass.InnerStaticInterface.i,  4+0 );
        Tester.checkEqual(Statics.NestedStaticClass.InnerStaticInterface.l,  4+0L);
        Tester.checkEqual(Statics.NestedStaticClass.InnerStaticInterface.f, (float) 4+0 );
        Tester.checkEqual(Statics.NestedStaticClass.InnerStaticInterface.d, (double) 4+0 );
        Tester.checkEqual(Statics.NestedStaticClass.InnerStaticInterface.c, '4'+0);

        Tester.checkEqual(Statics.NestedStaticClass.InnerInterface.I, new Integer(5+0));
        Tester.checkEqual(Statics.NestedStaticClass.InnerInterface.b, (byte) 5+0 );
        Tester.checkEqual(Statics.NestedStaticClass.InnerInterface.s, (short) 5+0 );
        Tester.checkEqual(Statics.NestedStaticClass.InnerInterface.i,  5+0 );
        Tester.checkEqual(Statics.NestedStaticClass.InnerInterface.l,  5+0L);
        Tester.checkEqual(Statics.NestedStaticClass.InnerInterface.f, (float) 5+0 );
        Tester.checkEqual(Statics.NestedStaticClass.InnerInterface.d, (double) 5+0 );
        Tester.checkEqual(Statics.NestedStaticClass.InnerInterface.c, '5'+0);

        Tester.checkEqual(Statics.NestedStaticClass.InnerStaticInterfaceNoStatics.I, new Integer(6+0));
        Tester.checkEqual(Statics.NestedStaticClass.InnerStaticInterfaceNoStatics.b, (byte) 6+0 );
        Tester.checkEqual(Statics.NestedStaticClass.InnerStaticInterfaceNoStatics.s, (short) 6+0 );
        Tester.checkEqual(Statics.NestedStaticClass.InnerStaticInterfaceNoStatics.i,  6+0 );
        Tester.checkEqual(Statics.NestedStaticClass.InnerStaticInterfaceNoStatics.l,  6+0L);
        Tester.checkEqual(Statics.NestedStaticClass.InnerStaticInterfaceNoStatics.f, (float) 6+0 );
        Tester.checkEqual(Statics.NestedStaticClass.InnerStaticInterfaceNoStatics.d, (double) 6+0 );
        Tester.checkEqual(Statics.NestedStaticClass.InnerStaticInterfaceNoStatics.c, '6'+0);

        Tester.checkEqual(Statics.NestedStaticClass.InnerInterfaceNoStatics.I, new Integer(7+0));
        Tester.checkEqual(Statics.NestedStaticClass.InnerInterfaceNoStatics.b, (byte) 7+0 );
        Tester.checkEqual(Statics.NestedStaticClass.InnerInterfaceNoStatics.s, (short) 7+0 );
        Tester.checkEqual(Statics.NestedStaticClass.InnerInterfaceNoStatics.i,  7+0 );
        Tester.checkEqual(Statics.NestedStaticClass.InnerInterfaceNoStatics.l,  7+0L);
        Tester.checkEqual(Statics.NestedStaticClass.InnerInterfaceNoStatics.f, (float) 7+0 );
        Tester.checkEqual(Statics.NestedStaticClass.InnerInterfaceNoStatics.d, (double) 7+0 );
        Tester.checkEqual(Statics.NestedStaticClass.InnerInterfaceNoStatics.c, '7'+0);

        // Statics.NestedStaticInterface
        Tester.checkEqual(Statics.NestedStaticInterface.I, new Integer(2+10));
        Tester.checkEqual(Statics.NestedStaticInterface.b, (byte) 2+10 );
        Tester.checkEqual(Statics.NestedStaticInterface.s, (short) 2+10 );
        Tester.checkEqual(Statics.NestedStaticInterface.i,  2+10 );
        Tester.checkEqual(Statics.NestedStaticInterface.l,  2+10L);
        Tester.checkEqual(Statics.NestedStaticInterface.f, (float) 2+10 );
        Tester.checkEqual(Statics.NestedStaticInterface.d, (double) 2+10 );
        Tester.checkEqual(Statics.NestedStaticInterface.c, '2'+10);

        Tester.checkEqual(Statics.NestedStaticInterface.InnerStaticClass.I, new Integer(3+10));
        Tester.checkEqual(Statics.NestedStaticInterface.InnerStaticClass.b, (byte) 3+10 );
        Tester.checkEqual(Statics.NestedStaticInterface.InnerStaticClass.s, (short) 3+10 );
        Tester.checkEqual(Statics.NestedStaticInterface.InnerStaticClass.i,  3+10 );
        Tester.checkEqual(Statics.NestedStaticInterface.InnerStaticClass.l,  3+10L);
        Tester.checkEqual(Statics.NestedStaticInterface.InnerStaticClass.f, (float) 3+10 );
        Tester.checkEqual(Statics.NestedStaticInterface.InnerStaticClass.d, (double) 3+10 );
        Tester.checkEqual(Statics.NestedStaticInterface.InnerStaticClass.c, '3'+10);

        Tester.checkEqual(Statics.NestedStaticInterface.InnerStaticInterface.I, new Integer(4+10));
        Tester.checkEqual(Statics.NestedStaticInterface.InnerStaticInterface.b, (byte) 4+10 );
        Tester.checkEqual(Statics.NestedStaticInterface.InnerStaticInterface.s, (short) 4+10 );
        Tester.checkEqual(Statics.NestedStaticInterface.InnerStaticInterface.i,  4+10 );
        Tester.checkEqual(Statics.NestedStaticInterface.InnerStaticInterface.l,  4+10L);
        Tester.checkEqual(Statics.NestedStaticInterface.InnerStaticInterface.f, (float) 4+10 );
        Tester.checkEqual(Statics.NestedStaticInterface.InnerStaticInterface.d, (double) 4+10 );
        Tester.checkEqual(Statics.NestedStaticInterface.InnerStaticInterface.c, '4'+10);

        Tester.checkEqual(Statics.NestedStaticInterface.InnerInterface.I, new Integer(5+10));
        Tester.checkEqual(Statics.NestedStaticInterface.InnerInterface.b, (byte) 5+10 );
        Tester.checkEqual(Statics.NestedStaticInterface.InnerInterface.s, (short) 5+10 );
        Tester.checkEqual(Statics.NestedStaticInterface.InnerInterface.i,  5+10 );
        Tester.checkEqual(Statics.NestedStaticInterface.InnerInterface.l,  5+10L);
        Tester.checkEqual(Statics.NestedStaticInterface.InnerInterface.f, (float) 5+10 );
        Tester.checkEqual(Statics.NestedStaticInterface.InnerInterface.d, (double) 5+10 );
        Tester.checkEqual(Statics.NestedStaticInterface.InnerInterface.c, '5'+10);

        Tester.checkEqual(Statics.NestedStaticInterface.InnerStaticInterfaceNoStatics.I, new Integer(6+10));
        Tester.checkEqual(Statics.NestedStaticInterface.InnerStaticInterfaceNoStatics.b, (byte) 6+10 );
        Tester.checkEqual(Statics.NestedStaticInterface.InnerStaticInterfaceNoStatics.s, (short) 6+10 );
        Tester.checkEqual(Statics.NestedStaticInterface.InnerStaticInterfaceNoStatics.i,  6+10 );
        Tester.checkEqual(Statics.NestedStaticInterface.InnerStaticInterfaceNoStatics.l,  6+10L);
        Tester.checkEqual(Statics.NestedStaticInterface.InnerStaticInterfaceNoStatics.f, (float) 6+10 );
        Tester.checkEqual(Statics.NestedStaticInterface.InnerStaticInterfaceNoStatics.d, (double) 6+10 );
        Tester.checkEqual(Statics.NestedStaticInterface.InnerStaticInterfaceNoStatics.c, '6'+10);

        Tester.checkEqual(Statics.NestedStaticInterface.InnerInterfaceNoStatics.I, new Integer(7+10));
        Tester.checkEqual(Statics.NestedStaticInterface.InnerInterfaceNoStatics.b, (byte) 7+10 );
        Tester.checkEqual(Statics.NestedStaticInterface.InnerInterfaceNoStatics.s, (short) 7+10 );
        Tester.checkEqual(Statics.NestedStaticInterface.InnerInterfaceNoStatics.i,  7+10 );
        Tester.checkEqual(Statics.NestedStaticInterface.InnerInterfaceNoStatics.l,  7+10L);
        Tester.checkEqual(Statics.NestedStaticInterface.InnerInterfaceNoStatics.f, (float) 7+10 );
        Tester.checkEqual(Statics.NestedStaticInterface.InnerInterfaceNoStatics.d, (double) 7+10 );
        Tester.checkEqual(Statics.NestedStaticInterface.InnerInterfaceNoStatics.c, '7'+10);

        // Statics.NestedInterface
        Tester.checkEqual(Statics.NestedInterface.I, new Integer(2+20));
        Tester.checkEqual(Statics.NestedInterface.b, (byte) 2+20 );
        Tester.checkEqual(Statics.NestedInterface.s, (short) 2+20 );
        Tester.checkEqual(Statics.NestedInterface.i,  2+20 );
        Tester.checkEqual(Statics.NestedInterface.l,  2+20L);
        Tester.checkEqual(Statics.NestedInterface.f, (float) 2+20 );
        Tester.checkEqual(Statics.NestedInterface.d, (double) 2+20 );
        Tester.checkEqual(Statics.NestedInterface.c, '2'+20);

        Tester.checkEqual(Statics.NestedInterface.InnerStaticClass.I, new Integer(3+20));
        Tester.checkEqual(Statics.NestedInterface.InnerStaticClass.b, (byte) 3+20 );
        Tester.checkEqual(Statics.NestedInterface.InnerStaticClass.s, (short) 3+20 );
        Tester.checkEqual(Statics.NestedInterface.InnerStaticClass.i,  3+20 );
        Tester.checkEqual(Statics.NestedInterface.InnerStaticClass.l,  3+20L);
        Tester.checkEqual(Statics.NestedInterface.InnerStaticClass.f, (float) 3+20 );
        Tester.checkEqual(Statics.NestedInterface.InnerStaticClass.d, (double) 3+20 );
        Tester.checkEqual(Statics.NestedInterface.InnerStaticClass.c, '3'+20);

        Tester.checkEqual(Statics.NestedInterface.InnerStaticInterface.I, new Integer(4+20));
        Tester.checkEqual(Statics.NestedInterface.InnerStaticInterface.b, (byte) 4+20 );
        Tester.checkEqual(Statics.NestedInterface.InnerStaticInterface.s, (short) 4+20 );
        Tester.checkEqual(Statics.NestedInterface.InnerStaticInterface.i,  4+20 );
        Tester.checkEqual(Statics.NestedInterface.InnerStaticInterface.l,  4+20L);
        Tester.checkEqual(Statics.NestedInterface.InnerStaticInterface.f, (float) 4+20 );
        Tester.checkEqual(Statics.NestedInterface.InnerStaticInterface.d, (double) 4+20 );
        Tester.checkEqual(Statics.NestedInterface.InnerStaticInterface.c, '4'+20);

        Tester.checkEqual(Statics.NestedInterface.InnerInterface.I, new Integer(5+20));
        Tester.checkEqual(Statics.NestedInterface.InnerInterface.b, (byte) 5+20 );
        Tester.checkEqual(Statics.NestedInterface.InnerInterface.s, (short) 5+20 );
        Tester.checkEqual(Statics.NestedInterface.InnerInterface.i,  5+20 );
        Tester.checkEqual(Statics.NestedInterface.InnerInterface.l,  5+20L);
        Tester.checkEqual(Statics.NestedInterface.InnerInterface.f, (float) 5+20 );
        Tester.checkEqual(Statics.NestedInterface.InnerInterface.d, (double) 5+20 );
        Tester.checkEqual(Statics.NestedInterface.InnerInterface.c, '5'+20);

        Tester.checkEqual(Statics.NestedInterface.InnerStaticInterfaceNoStatics.I, new Integer(6+20));
        Tester.checkEqual(Statics.NestedInterface.InnerStaticInterfaceNoStatics.b, (byte) 6+20 );
        Tester.checkEqual(Statics.NestedInterface.InnerStaticInterfaceNoStatics.s, (short) 6+20 );
        Tester.checkEqual(Statics.NestedInterface.InnerStaticInterfaceNoStatics.i,  6+20 );
        Tester.checkEqual(Statics.NestedInterface.InnerStaticInterfaceNoStatics.l,  6+20L);
        Tester.checkEqual(Statics.NestedInterface.InnerStaticInterfaceNoStatics.f, (float) 6+20 );
        Tester.checkEqual(Statics.NestedInterface.InnerStaticInterfaceNoStatics.d, (double) 6+20 );
        Tester.checkEqual(Statics.NestedInterface.InnerStaticInterfaceNoStatics.c, '6'+20);

        Tester.checkEqual(Statics.NestedInterface.InnerInterfaceNoStatics.I, new Integer(7+20));
        Tester.checkEqual(Statics.NestedInterface.InnerInterfaceNoStatics.b, (byte) 7+20 );
        Tester.checkEqual(Statics.NestedInterface.InnerInterfaceNoStatics.s, (short) 7+20 );
        Tester.checkEqual(Statics.NestedInterface.InnerInterfaceNoStatics.i,  7+20 );
        Tester.checkEqual(Statics.NestedInterface.InnerInterfaceNoStatics.l,  7+20L);
        Tester.checkEqual(Statics.NestedInterface.InnerInterfaceNoStatics.f, (float) 7+20 );
        Tester.checkEqual(Statics.NestedInterface.InnerInterfaceNoStatics.d, (double) 7+20 );
        Tester.checkEqual(Statics.NestedInterface.InnerInterfaceNoStatics.c, '7'+20);


        // --- p1.P1Statics
        Tester.checkEqual(p1.P1Statics.I, new Integer(1+1));
        Tester.checkEqual(p1.P1Statics.b, (byte) 1+1 );
        Tester.checkEqual(p1.P1Statics.s, (short) 1+1 );
        Tester.checkEqual(p1.P1Statics.i,  1+1 );
        Tester.checkEqual(p1.P1Statics.l,  1+1L);
        Tester.checkEqual(p1.P1Statics.f, (float) 1+1 );
        Tester.checkEqual(p1.P1Statics.d, (double) 1+1 );
        Tester.checkEqual(p1.P1Statics.c, '1'+1);

        // p1.P1Statics.NestedStaticClass
        Tester.checkEqual(p1.P1Statics.NestedStaticClass.I, new Integer(2+1));
        Tester.checkEqual(p1.P1Statics.NestedStaticClass.b, (byte) 2+1 );
        Tester.checkEqual(p1.P1Statics.NestedStaticClass.s, (short) 2+1 );
        Tester.checkEqual(p1.P1Statics.NestedStaticClass.i,  2+1 );
        Tester.checkEqual(p1.P1Statics.NestedStaticClass.l,  2+1L);
        Tester.checkEqual(p1.P1Statics.NestedStaticClass.f, (float) 2+1 );
        Tester.checkEqual(p1.P1Statics.NestedStaticClass.d, (double) 2+1 );
        Tester.checkEqual(p1.P1Statics.NestedStaticClass.c, '2'+1);

        Tester.checkEqual(p1.P1Statics.NestedStaticClass.InnerStaticClass.I, new Integer(3+1));
        Tester.checkEqual(p1.P1Statics.NestedStaticClass.InnerStaticClass.b, (byte) 3+1 );
        Tester.checkEqual(p1.P1Statics.NestedStaticClass.InnerStaticClass.s, (short) 3+1 );
        Tester.checkEqual(p1.P1Statics.NestedStaticClass.InnerStaticClass.i,  3+1 );
        Tester.checkEqual(p1.P1Statics.NestedStaticClass.InnerStaticClass.l,  3+1L);
        Tester.checkEqual(p1.P1Statics.NestedStaticClass.InnerStaticClass.f, (float) 3+1 );
        Tester.checkEqual(p1.P1Statics.NestedStaticClass.InnerStaticClass.d, (double) 3+1 );
        Tester.checkEqual(p1.P1Statics.NestedStaticClass.InnerStaticClass.c, '3'+1);

        Tester.checkEqual(p1.P1Statics.NestedStaticClass.InnerStaticInterface.I, new Integer(4+1));
        Tester.checkEqual(p1.P1Statics.NestedStaticClass.InnerStaticInterface.b, (byte) 4+1 );
        Tester.checkEqual(p1.P1Statics.NestedStaticClass.InnerStaticInterface.s, (short) 4+1 );
        Tester.checkEqual(p1.P1Statics.NestedStaticClass.InnerStaticInterface.i,  4+1 );
        Tester.checkEqual(p1.P1Statics.NestedStaticClass.InnerStaticInterface.l,  4+1L);
        Tester.checkEqual(p1.P1Statics.NestedStaticClass.InnerStaticInterface.f, (float) 4+1 );
        Tester.checkEqual(p1.P1Statics.NestedStaticClass.InnerStaticInterface.d, (double) 4+1 );
        Tester.checkEqual(p1.P1Statics.NestedStaticClass.InnerStaticInterface.c, '4'+1);

        Tester.checkEqual(p1.P1Statics.NestedStaticClass.InnerInterface.I, new Integer(5+1));
        Tester.checkEqual(p1.P1Statics.NestedStaticClass.InnerInterface.b, (byte) 5+1 );
        Tester.checkEqual(p1.P1Statics.NestedStaticClass.InnerInterface.s, (short) 5+1 );
        Tester.checkEqual(p1.P1Statics.NestedStaticClass.InnerInterface.i,  5+1 );
        Tester.checkEqual(p1.P1Statics.NestedStaticClass.InnerInterface.l,  5+1L);
        Tester.checkEqual(p1.P1Statics.NestedStaticClass.InnerInterface.f, (float) 5+1 );
        Tester.checkEqual(p1.P1Statics.NestedStaticClass.InnerInterface.d, (double) 5+1 );
        Tester.checkEqual(p1.P1Statics.NestedStaticClass.InnerInterface.c, '5'+1);

        Tester.checkEqual(p1.P1Statics.NestedStaticClass.InnerStaticInterfaceNoStatics.I, new Integer(6+1));
        Tester.checkEqual(p1.P1Statics.NestedStaticClass.InnerStaticInterfaceNoStatics.b, (byte) 6+1 );
        Tester.checkEqual(p1.P1Statics.NestedStaticClass.InnerStaticInterfaceNoStatics.s, (short) 6+1 );
        Tester.checkEqual(p1.P1Statics.NestedStaticClass.InnerStaticInterfaceNoStatics.i,  6+1 );
        Tester.checkEqual(p1.P1Statics.NestedStaticClass.InnerStaticInterfaceNoStatics.l,  6+1L);
        Tester.checkEqual(p1.P1Statics.NestedStaticClass.InnerStaticInterfaceNoStatics.f, (float) 6+1 );
        Tester.checkEqual(p1.P1Statics.NestedStaticClass.InnerStaticInterfaceNoStatics.d, (double) 6+1 );
        Tester.checkEqual(p1.P1Statics.NestedStaticClass.InnerStaticInterfaceNoStatics.c, '6'+1);

        Tester.checkEqual(p1.P1Statics.NestedStaticClass.InnerInterfaceNoStatics.I, new Integer(7+1));
        Tester.checkEqual(p1.P1Statics.NestedStaticClass.InnerInterfaceNoStatics.b, (byte) 7+1 );
        Tester.checkEqual(p1.P1Statics.NestedStaticClass.InnerInterfaceNoStatics.s, (short) 7+1 );
        Tester.checkEqual(p1.P1Statics.NestedStaticClass.InnerInterfaceNoStatics.i,  7+1 );
        Tester.checkEqual(p1.P1Statics.NestedStaticClass.InnerInterfaceNoStatics.l,  7+1L);
        Tester.checkEqual(p1.P1Statics.NestedStaticClass.InnerInterfaceNoStatics.f, (float) 7+1 );
        Tester.checkEqual(p1.P1Statics.NestedStaticClass.InnerInterfaceNoStatics.d, (double) 7+1 );
        Tester.checkEqual(p1.P1Statics.NestedStaticClass.InnerInterfaceNoStatics.c, '7'+1);

        // p1.P1Statics.NestedStaticInterface
        Tester.checkEqual(p1.P1Statics.NestedStaticInterface.I, new Integer(2+11));
        Tester.checkEqual(p1.P1Statics.NestedStaticInterface.b, (byte) 2+11 );
        Tester.checkEqual(p1.P1Statics.NestedStaticInterface.s, (short) 2+11 );
        Tester.checkEqual(p1.P1Statics.NestedStaticInterface.i,  2+11 );
        Tester.checkEqual(p1.P1Statics.NestedStaticInterface.l,  2+11L);
        Tester.checkEqual(p1.P1Statics.NestedStaticInterface.f, (float) 2+11 );
        Tester.checkEqual(p1.P1Statics.NestedStaticInterface.d, (double) 2+11 );
        Tester.checkEqual(p1.P1Statics.NestedStaticInterface.c, '2'+11);

        Tester.checkEqual(p1.P1Statics.NestedStaticInterface.InnerStaticClass.I, new Integer(3+11));
        Tester.checkEqual(p1.P1Statics.NestedStaticInterface.InnerStaticClass.b, (byte) 3+11 );
        Tester.checkEqual(p1.P1Statics.NestedStaticInterface.InnerStaticClass.s, (short) 3+11 );
        Tester.checkEqual(p1.P1Statics.NestedStaticInterface.InnerStaticClass.i,  3+11 );
        Tester.checkEqual(p1.P1Statics.NestedStaticInterface.InnerStaticClass.l,  3+11L);
        Tester.checkEqual(p1.P1Statics.NestedStaticInterface.InnerStaticClass.f, (float) 3+11 );
        Tester.checkEqual(p1.P1Statics.NestedStaticInterface.InnerStaticClass.d, (double) 3+11 );
        Tester.checkEqual(p1.P1Statics.NestedStaticInterface.InnerStaticClass.c, '3'+11);

        Tester.checkEqual(p1.P1Statics.NestedStaticInterface.InnerStaticInterface.I, new Integer(4+11));
        Tester.checkEqual(p1.P1Statics.NestedStaticInterface.InnerStaticInterface.b, (byte) 4+11 );
        Tester.checkEqual(p1.P1Statics.NestedStaticInterface.InnerStaticInterface.s, (short) 4+11 );
        Tester.checkEqual(p1.P1Statics.NestedStaticInterface.InnerStaticInterface.i,  4+11 );
        Tester.checkEqual(p1.P1Statics.NestedStaticInterface.InnerStaticInterface.l,  4+11L);
        Tester.checkEqual(p1.P1Statics.NestedStaticInterface.InnerStaticInterface.f, (float) 4+11 );
        Tester.checkEqual(p1.P1Statics.NestedStaticInterface.InnerStaticInterface.d, (double) 4+11 );
        Tester.checkEqual(p1.P1Statics.NestedStaticInterface.InnerStaticInterface.c, '4'+11);

        Tester.checkEqual(p1.P1Statics.NestedStaticInterface.InnerInterface.I, new Integer(5+11));
        Tester.checkEqual(p1.P1Statics.NestedStaticInterface.InnerInterface.b, (byte) 5+11 );
        Tester.checkEqual(p1.P1Statics.NestedStaticInterface.InnerInterface.s, (short) 5+11 );
        Tester.checkEqual(p1.P1Statics.NestedStaticInterface.InnerInterface.i,  5+11 );
        Tester.checkEqual(p1.P1Statics.NestedStaticInterface.InnerInterface.l,  5+11L);
        Tester.checkEqual(p1.P1Statics.NestedStaticInterface.InnerInterface.f, (float) 5+11 );
        Tester.checkEqual(p1.P1Statics.NestedStaticInterface.InnerInterface.d, (double) 5+11 );
        Tester.checkEqual(p1.P1Statics.NestedStaticInterface.InnerInterface.c, '5'+11);

        Tester.checkEqual(p1.P1Statics.NestedStaticInterface.InnerStaticInterfaceNoStatics.I, new Integer(6+11));
        Tester.checkEqual(p1.P1Statics.NestedStaticInterface.InnerStaticInterfaceNoStatics.b, (byte) 6+11 );
        Tester.checkEqual(p1.P1Statics.NestedStaticInterface.InnerStaticInterfaceNoStatics.s, (short) 6+11 );
        Tester.checkEqual(p1.P1Statics.NestedStaticInterface.InnerStaticInterfaceNoStatics.i,  6+11 );
        Tester.checkEqual(p1.P1Statics.NestedStaticInterface.InnerStaticInterfaceNoStatics.l,  6+11L);
        Tester.checkEqual(p1.P1Statics.NestedStaticInterface.InnerStaticInterfaceNoStatics.f, (float) 6+11 );
        Tester.checkEqual(p1.P1Statics.NestedStaticInterface.InnerStaticInterfaceNoStatics.d, (double) 6+11 );
        Tester.checkEqual(p1.P1Statics.NestedStaticInterface.InnerStaticInterfaceNoStatics.c, '6'+11);

        Tester.checkEqual(p1.P1Statics.NestedStaticInterface.InnerInterfaceNoStatics.I, new Integer(7+11));
        Tester.checkEqual(p1.P1Statics.NestedStaticInterface.InnerInterfaceNoStatics.b, (byte) 7+11 );
        Tester.checkEqual(p1.P1Statics.NestedStaticInterface.InnerInterfaceNoStatics.s, (short) 7+11 );
        Tester.checkEqual(p1.P1Statics.NestedStaticInterface.InnerInterfaceNoStatics.i,  7+11 );
        Tester.checkEqual(p1.P1Statics.NestedStaticInterface.InnerInterfaceNoStatics.l,  7+11L);
        Tester.checkEqual(p1.P1Statics.NestedStaticInterface.InnerInterfaceNoStatics.f, (float) 7+11 );
        Tester.checkEqual(p1.P1Statics.NestedStaticInterface.InnerInterfaceNoStatics.d, (double) 7+11 );
        Tester.checkEqual(p1.P1Statics.NestedStaticInterface.InnerInterfaceNoStatics.c, '7'+11);

        // p1.P1Statics.NestedInterface
        Tester.checkEqual(p1.P1Statics.NestedInterface.I, new Integer(2+21));
        Tester.checkEqual(p1.P1Statics.NestedInterface.b, (byte) 2+21 );
        Tester.checkEqual(p1.P1Statics.NestedInterface.s, (short) 2+21 );
        Tester.checkEqual(p1.P1Statics.NestedInterface.i,  2+21 );
        Tester.checkEqual(p1.P1Statics.NestedInterface.l,  2+21L);
        Tester.checkEqual(p1.P1Statics.NestedInterface.f, (float) 2+21 );
        Tester.checkEqual(p1.P1Statics.NestedInterface.d, (double) 2+21 );
        Tester.checkEqual(p1.P1Statics.NestedInterface.c, '2'+21);

        Tester.checkEqual(p1.P1Statics.NestedInterface.InnerStaticClass.I, new Integer(3+21));
        Tester.checkEqual(p1.P1Statics.NestedInterface.InnerStaticClass.b, (byte) 3+21 );
        Tester.checkEqual(p1.P1Statics.NestedInterface.InnerStaticClass.s, (short) 3+21 );
        Tester.checkEqual(p1.P1Statics.NestedInterface.InnerStaticClass.i,  3+21 );
        Tester.checkEqual(p1.P1Statics.NestedInterface.InnerStaticClass.l,  3+21L);
        Tester.checkEqual(p1.P1Statics.NestedInterface.InnerStaticClass.f, (float) 3+21 );
        Tester.checkEqual(p1.P1Statics.NestedInterface.InnerStaticClass.d, (double) 3+21 );
        Tester.checkEqual(p1.P1Statics.NestedInterface.InnerStaticClass.c, '3'+21);

        Tester.checkEqual(p1.P1Statics.NestedInterface.InnerStaticInterface.I, new Integer(4+21));
        Tester.checkEqual(p1.P1Statics.NestedInterface.InnerStaticInterface.b, (byte) 4+21 );
        Tester.checkEqual(p1.P1Statics.NestedInterface.InnerStaticInterface.s, (short) 4+21 );
        Tester.checkEqual(p1.P1Statics.NestedInterface.InnerStaticInterface.i,  4+21 );
        Tester.checkEqual(p1.P1Statics.NestedInterface.InnerStaticInterface.l,  4+21L);
        Tester.checkEqual(p1.P1Statics.NestedInterface.InnerStaticInterface.f, (float) 4+21 );
        Tester.checkEqual(p1.P1Statics.NestedInterface.InnerStaticInterface.d, (double) 4+21 );
        Tester.checkEqual(p1.P1Statics.NestedInterface.InnerStaticInterface.c, '4'+21);

        Tester.checkEqual(p1.P1Statics.NestedInterface.InnerInterface.I, new Integer(5+21));
        Tester.checkEqual(p1.P1Statics.NestedInterface.InnerInterface.b, (byte) 5+21 );
        Tester.checkEqual(p1.P1Statics.NestedInterface.InnerInterface.s, (short) 5+21 );
        Tester.checkEqual(p1.P1Statics.NestedInterface.InnerInterface.i,  5+21 );
        Tester.checkEqual(p1.P1Statics.NestedInterface.InnerInterface.l,  5+21L);
        Tester.checkEqual(p1.P1Statics.NestedInterface.InnerInterface.f, (float) 5+21 );
        Tester.checkEqual(p1.P1Statics.NestedInterface.InnerInterface.d, (double) 5+21 );
        Tester.checkEqual(p1.P1Statics.NestedInterface.InnerInterface.c, '5'+21);

        Tester.checkEqual(p1.P1Statics.NestedInterface.InnerStaticInterfaceNoStatics.I, new Integer(6+21));
        Tester.checkEqual(p1.P1Statics.NestedInterface.InnerStaticInterfaceNoStatics.b, (byte) 6+21 );
        Tester.checkEqual(p1.P1Statics.NestedInterface.InnerStaticInterfaceNoStatics.s, (short) 6+21 );
        Tester.checkEqual(p1.P1Statics.NestedInterface.InnerStaticInterfaceNoStatics.i,  6+21 );
        Tester.checkEqual(p1.P1Statics.NestedInterface.InnerStaticInterfaceNoStatics.l,  6+21L);
        Tester.checkEqual(p1.P1Statics.NestedInterface.InnerStaticInterfaceNoStatics.f, (float) 6+21 );
        Tester.checkEqual(p1.P1Statics.NestedInterface.InnerStaticInterfaceNoStatics.d, (double) 6+21 );
        Tester.checkEqual(p1.P1Statics.NestedInterface.InnerStaticInterfaceNoStatics.c, '6'+21);

        Tester.checkEqual(p1.P1Statics.NestedInterface.InnerInterfaceNoStatics.I, new Integer(7+21));
        Tester.checkEqual(p1.P1Statics.NestedInterface.InnerInterfaceNoStatics.b, (byte) 7+21 );
        Tester.checkEqual(p1.P1Statics.NestedInterface.InnerInterfaceNoStatics.s, (short) 7+21 );
        Tester.checkEqual(p1.P1Statics.NestedInterface.InnerInterfaceNoStatics.i,  7+21 );
        Tester.checkEqual(p1.P1Statics.NestedInterface.InnerInterfaceNoStatics.l,  7+21L);
        Tester.checkEqual(p1.P1Statics.NestedInterface.InnerInterfaceNoStatics.f, (float) 7+21 );
        Tester.checkEqual(p1.P1Statics.NestedInterface.InnerInterfaceNoStatics.d, (double) 7+21 );
        Tester.checkEqual(p1.P1Statics.NestedInterface.InnerInterfaceNoStatics.c, '7'+21);


        // --- p1.p2.P1P2Statics
        Tester.checkEqual(p1.p2.P1P2Statics.I, new Integer(1+2));
        Tester.checkEqual(p1.p2.P1P2Statics.b, (byte) 1+2 );
        Tester.checkEqual(p1.p2.P1P2Statics.s, (short) 1+2 );
        Tester.checkEqual(p1.p2.P1P2Statics.i,  1+2 );
        Tester.checkEqual(p1.p2.P1P2Statics.l,  1+2L);
        Tester.checkEqual(p1.p2.P1P2Statics.f, (float) 1+2 );
        Tester.checkEqual(p1.p2.P1P2Statics.d, (double) 1+2 );
        Tester.checkEqual(p1.p2.P1P2Statics.c, '1'+2);

        // p1.p2.P1P2Statics.NestedStaticClass
        Tester.checkEqual(p1.p2.P1P2Statics.NestedStaticClass.I, new Integer(2+2));
        Tester.checkEqual(p1.p2.P1P2Statics.NestedStaticClass.b, (byte) 2+2 );
        Tester.checkEqual(p1.p2.P1P2Statics.NestedStaticClass.s, (short) 2+2 );
        Tester.checkEqual(p1.p2.P1P2Statics.NestedStaticClass.i,  2+2 );
        Tester.checkEqual(p1.p2.P1P2Statics.NestedStaticClass.l,  2+2L);
        Tester.checkEqual(p1.p2.P1P2Statics.NestedStaticClass.f, (float) 2+2 );
        Tester.checkEqual(p1.p2.P1P2Statics.NestedStaticClass.d, (double) 2+2 );
        Tester.checkEqual(p1.p2.P1P2Statics.NestedStaticClass.c, '2'+2);

        Tester.checkEqual(p1.p2.P1P2Statics.NestedStaticClass.InnerStaticClass.I, new Integer(3+2));
        Tester.checkEqual(p1.p2.P1P2Statics.NestedStaticClass.InnerStaticClass.b, (byte) 3+2 );
        Tester.checkEqual(p1.p2.P1P2Statics.NestedStaticClass.InnerStaticClass.s, (short) 3+2 );
        Tester.checkEqual(p1.p2.P1P2Statics.NestedStaticClass.InnerStaticClass.i,  3+2 );
        Tester.checkEqual(p1.p2.P1P2Statics.NestedStaticClass.InnerStaticClass.l,  3+2L);
        Tester.checkEqual(p1.p2.P1P2Statics.NestedStaticClass.InnerStaticClass.f, (float) 3+2 );
        Tester.checkEqual(p1.p2.P1P2Statics.NestedStaticClass.InnerStaticClass.d, (double) 3+2 );
        Tester.checkEqual(p1.p2.P1P2Statics.NestedStaticClass.InnerStaticClass.c, '3'+2);

        Tester.checkEqual(p1.p2.P1P2Statics.NestedStaticClass.InnerStaticInterface.I, new Integer(4+2));
        Tester.checkEqual(p1.p2.P1P2Statics.NestedStaticClass.InnerStaticInterface.b, (byte) 4+2 );
        Tester.checkEqual(p1.p2.P1P2Statics.NestedStaticClass.InnerStaticInterface.s, (short) 4+2 );
        Tester.checkEqual(p1.p2.P1P2Statics.NestedStaticClass.InnerStaticInterface.i,  4+2 );
        Tester.checkEqual(p1.p2.P1P2Statics.NestedStaticClass.InnerStaticInterface.l,  4+2L);
        Tester.checkEqual(p1.p2.P1P2Statics.NestedStaticClass.InnerStaticInterface.f, (float) 4+2 );
        Tester.checkEqual(p1.p2.P1P2Statics.NestedStaticClass.InnerStaticInterface.d, (double) 4+2 );
        Tester.checkEqual(p1.p2.P1P2Statics.NestedStaticClass.InnerStaticInterface.c, '4'+2);

        Tester.checkEqual(p1.p2.P1P2Statics.NestedStaticClass.InnerInterface.I, new Integer(5+2));
        Tester.checkEqual(p1.p2.P1P2Statics.NestedStaticClass.InnerInterface.b, (byte) 5+2 );
        Tester.checkEqual(p1.p2.P1P2Statics.NestedStaticClass.InnerInterface.s, (short) 5+2 );
        Tester.checkEqual(p1.p2.P1P2Statics.NestedStaticClass.InnerInterface.i,  5+2 );
        Tester.checkEqual(p1.p2.P1P2Statics.NestedStaticClass.InnerInterface.l,  5+2L);
        Tester.checkEqual(p1.p2.P1P2Statics.NestedStaticClass.InnerInterface.f, (float) 5+2 );
        Tester.checkEqual(p1.p2.P1P2Statics.NestedStaticClass.InnerInterface.d, (double) 5+2 );
        Tester.checkEqual(p1.p2.P1P2Statics.NestedStaticClass.InnerInterface.c, '5'+2);

        Tester.checkEqual(p1.p2.P1P2Statics.NestedStaticClass.InnerStaticInterfaceNoStatics.I, new Integer(6+2));
        Tester.checkEqual(p1.p2.P1P2Statics.NestedStaticClass.InnerStaticInterfaceNoStatics.b, (byte) 6+2 );
        Tester.checkEqual(p1.p2.P1P2Statics.NestedStaticClass.InnerStaticInterfaceNoStatics.s, (short) 6+2 );
        Tester.checkEqual(p1.p2.P1P2Statics.NestedStaticClass.InnerStaticInterfaceNoStatics.i,  6+2 );
        Tester.checkEqual(p1.p2.P1P2Statics.NestedStaticClass.InnerStaticInterfaceNoStatics.l,  6+2L);
        Tester.checkEqual(p1.p2.P1P2Statics.NestedStaticClass.InnerStaticInterfaceNoStatics.f, (float) 6+2 );
        Tester.checkEqual(p1.p2.P1P2Statics.NestedStaticClass.InnerStaticInterfaceNoStatics.d, (double) 6+2 );
        Tester.checkEqual(p1.p2.P1P2Statics.NestedStaticClass.InnerStaticInterfaceNoStatics.c, '6'+2);

        Tester.checkEqual(p1.p2.P1P2Statics.NestedStaticClass.InnerInterfaceNoStatics.I, new Integer(7+2));
        Tester.checkEqual(p1.p2.P1P2Statics.NestedStaticClass.InnerInterfaceNoStatics.b, (byte) 7+2 );
        Tester.checkEqual(p1.p2.P1P2Statics.NestedStaticClass.InnerInterfaceNoStatics.s, (short) 7+2 );
        Tester.checkEqual(p1.p2.P1P2Statics.NestedStaticClass.InnerInterfaceNoStatics.i,  7+2 );
        Tester.checkEqual(p1.p2.P1P2Statics.NestedStaticClass.InnerInterfaceNoStatics.l,  7+2L);
        Tester.checkEqual(p1.p2.P1P2Statics.NestedStaticClass.InnerInterfaceNoStatics.f, (float) 7+2 );
        Tester.checkEqual(p1.p2.P1P2Statics.NestedStaticClass.InnerInterfaceNoStatics.d, (double) 7+2 );
        Tester.checkEqual(p1.p2.P1P2Statics.NestedStaticClass.InnerInterfaceNoStatics.c, '7'+2);

        // p1.p2.P1P2Statics.NestedStaticInterface
        Tester.checkEqual(p1.p2.P1P2Statics.NestedStaticInterface.I, new Integer(2+12));
        Tester.checkEqual(p1.p2.P1P2Statics.NestedStaticInterface.b, (byte) 2+12 );
        Tester.checkEqual(p1.p2.P1P2Statics.NestedStaticInterface.s, (short) 2+12 );
        Tester.checkEqual(p1.p2.P1P2Statics.NestedStaticInterface.i,  2+12 );
        Tester.checkEqual(p1.p2.P1P2Statics.NestedStaticInterface.l,  2+12L);
        Tester.checkEqual(p1.p2.P1P2Statics.NestedStaticInterface.f, (float) 2+12 );
        Tester.checkEqual(p1.p2.P1P2Statics.NestedStaticInterface.d, (double) 2+12 );
        Tester.checkEqual(p1.p2.P1P2Statics.NestedStaticInterface.c, '2'+12);

        Tester.checkEqual(p1.p2.P1P2Statics.NestedStaticInterface.InnerStaticClass.I, new Integer(3+12));
        Tester.checkEqual(p1.p2.P1P2Statics.NestedStaticInterface.InnerStaticClass.b, (byte) 3+12 );
        Tester.checkEqual(p1.p2.P1P2Statics.NestedStaticInterface.InnerStaticClass.s, (short) 3+12 );
        Tester.checkEqual(p1.p2.P1P2Statics.NestedStaticInterface.InnerStaticClass.i,  3+12 );
        Tester.checkEqual(p1.p2.P1P2Statics.NestedStaticInterface.InnerStaticClass.l,  3+12L);
        Tester.checkEqual(p1.p2.P1P2Statics.NestedStaticInterface.InnerStaticClass.f, (float) 3+12 );
        Tester.checkEqual(p1.p2.P1P2Statics.NestedStaticInterface.InnerStaticClass.d, (double) 3+12 );
        Tester.checkEqual(p1.p2.P1P2Statics.NestedStaticInterface.InnerStaticClass.c, '3'+12);

        Tester.checkEqual(p1.p2.P1P2Statics.NestedStaticInterface.InnerStaticInterface.I, new Integer(4+12));
        Tester.checkEqual(p1.p2.P1P2Statics.NestedStaticInterface.InnerStaticInterface.b, (byte) 4+12 );
        Tester.checkEqual(p1.p2.P1P2Statics.NestedStaticInterface.InnerStaticInterface.s, (short) 4+12 );
        Tester.checkEqual(p1.p2.P1P2Statics.NestedStaticInterface.InnerStaticInterface.i,  4+12 );
        Tester.checkEqual(p1.p2.P1P2Statics.NestedStaticInterface.InnerStaticInterface.l,  4+12L);
        Tester.checkEqual(p1.p2.P1P2Statics.NestedStaticInterface.InnerStaticInterface.f, (float) 4+12 );
        Tester.checkEqual(p1.p2.P1P2Statics.NestedStaticInterface.InnerStaticInterface.d, (double) 4+12 );
        Tester.checkEqual(p1.p2.P1P2Statics.NestedStaticInterface.InnerStaticInterface.c, '4'+12);

        Tester.checkEqual(p1.p2.P1P2Statics.NestedStaticInterface.InnerInterface.I, new Integer(5+12));
        Tester.checkEqual(p1.p2.P1P2Statics.NestedStaticInterface.InnerInterface.b, (byte) 5+12 );
        Tester.checkEqual(p1.p2.P1P2Statics.NestedStaticInterface.InnerInterface.s, (short) 5+12 );
        Tester.checkEqual(p1.p2.P1P2Statics.NestedStaticInterface.InnerInterface.i,  5+12 );
        Tester.checkEqual(p1.p2.P1P2Statics.NestedStaticInterface.InnerInterface.l,  5+12L);
        Tester.checkEqual(p1.p2.P1P2Statics.NestedStaticInterface.InnerInterface.f, (float) 5+12 );
        Tester.checkEqual(p1.p2.P1P2Statics.NestedStaticInterface.InnerInterface.d, (double) 5+12 );
        Tester.checkEqual(p1.p2.P1P2Statics.NestedStaticInterface.InnerInterface.c, '5'+12);

        Tester.checkEqual(p1.p2.P1P2Statics.NestedStaticInterface.InnerStaticInterfaceNoStatics.I, new Integer(6+12));
        Tester.checkEqual(p1.p2.P1P2Statics.NestedStaticInterface.InnerStaticInterfaceNoStatics.b, (byte) 6+12 );
        Tester.checkEqual(p1.p2.P1P2Statics.NestedStaticInterface.InnerStaticInterfaceNoStatics.s, (short) 6+12 );
        Tester.checkEqual(p1.p2.P1P2Statics.NestedStaticInterface.InnerStaticInterfaceNoStatics.i,  6+12 );
        Tester.checkEqual(p1.p2.P1P2Statics.NestedStaticInterface.InnerStaticInterfaceNoStatics.l,  6+12L);
        Tester.checkEqual(p1.p2.P1P2Statics.NestedStaticInterface.InnerStaticInterfaceNoStatics.f, (float) 6+12 );
        Tester.checkEqual(p1.p2.P1P2Statics.NestedStaticInterface.InnerStaticInterfaceNoStatics.d, (double) 6+12 );
        Tester.checkEqual(p1.p2.P1P2Statics.NestedStaticInterface.InnerStaticInterfaceNoStatics.c, '6'+12);

        Tester.checkEqual(p1.p2.P1P2Statics.NestedStaticInterface.InnerInterfaceNoStatics.I, new Integer(7+12));
        Tester.checkEqual(p1.p2.P1P2Statics.NestedStaticInterface.InnerInterfaceNoStatics.b, (byte) 7+12 );
        Tester.checkEqual(p1.p2.P1P2Statics.NestedStaticInterface.InnerInterfaceNoStatics.s, (short) 7+12 );
        Tester.checkEqual(p1.p2.P1P2Statics.NestedStaticInterface.InnerInterfaceNoStatics.i,  7+12 );
        Tester.checkEqual(p1.p2.P1P2Statics.NestedStaticInterface.InnerInterfaceNoStatics.l,  7+12L);
        Tester.checkEqual(p1.p2.P1P2Statics.NestedStaticInterface.InnerInterfaceNoStatics.f, (float) 7+12 );
        Tester.checkEqual(p1.p2.P1P2Statics.NestedStaticInterface.InnerInterfaceNoStatics.d, (double) 7+12 );
        Tester.checkEqual(p1.p2.P1P2Statics.NestedStaticInterface.InnerInterfaceNoStatics.c, '7'+12);

        // p1.p2.P1P2Statics.NestedInterface
        Tester.checkEqual(p1.p2.P1P2Statics.NestedInterface.I, new Integer(2+22));
        Tester.checkEqual(p1.p2.P1P2Statics.NestedInterface.b, (byte) 2+22 );
        Tester.checkEqual(p1.p2.P1P2Statics.NestedInterface.s, (short) 2+22 );
        Tester.checkEqual(p1.p2.P1P2Statics.NestedInterface.i,  2+22 );
        Tester.checkEqual(p1.p2.P1P2Statics.NestedInterface.l,  2+22L);
        Tester.checkEqual(p1.p2.P1P2Statics.NestedInterface.f, (float) 2+22 );
        Tester.checkEqual(p1.p2.P1P2Statics.NestedInterface.d, (double) 2+22 );
        Tester.checkEqual(p1.p2.P1P2Statics.NestedInterface.c, '2'+22);

        Tester.checkEqual(p1.p2.P1P2Statics.NestedInterface.InnerStaticClass.I, new Integer(3+22));
        Tester.checkEqual(p1.p2.P1P2Statics.NestedInterface.InnerStaticClass.b, (byte) 3+22 );
        Tester.checkEqual(p1.p2.P1P2Statics.NestedInterface.InnerStaticClass.s, (short) 3+22 );
        Tester.checkEqual(p1.p2.P1P2Statics.NestedInterface.InnerStaticClass.i,  3+22 );
        Tester.checkEqual(p1.p2.P1P2Statics.NestedInterface.InnerStaticClass.l,  3+22L);
        Tester.checkEqual(p1.p2.P1P2Statics.NestedInterface.InnerStaticClass.f, (float) 3+22 );
        Tester.checkEqual(p1.p2.P1P2Statics.NestedInterface.InnerStaticClass.d, (double) 3+22 );
        Tester.checkEqual(p1.p2.P1P2Statics.NestedInterface.InnerStaticClass.c, '3'+22);

        Tester.checkEqual(p1.p2.P1P2Statics.NestedInterface.InnerStaticInterface.I, new Integer(4+22));
        Tester.checkEqual(p1.p2.P1P2Statics.NestedInterface.InnerStaticInterface.b, (byte) 4+22 );
        Tester.checkEqual(p1.p2.P1P2Statics.NestedInterface.InnerStaticInterface.s, (short) 4+22 );
        Tester.checkEqual(p1.p2.P1P2Statics.NestedInterface.InnerStaticInterface.i,  4+22 );
        Tester.checkEqual(p1.p2.P1P2Statics.NestedInterface.InnerStaticInterface.l,  4+22L);
        Tester.checkEqual(p1.p2.P1P2Statics.NestedInterface.InnerStaticInterface.f, (float) 4+22 );
        Tester.checkEqual(p1.p2.P1P2Statics.NestedInterface.InnerStaticInterface.d, (double) 4+22 );
        Tester.checkEqual(p1.p2.P1P2Statics.NestedInterface.InnerStaticInterface.c, '4'+22);

        Tester.checkEqual(p1.p2.P1P2Statics.NestedInterface.InnerInterface.I, new Integer(5+22));
        Tester.checkEqual(p1.p2.P1P2Statics.NestedInterface.InnerInterface.b, (byte) 5+22 );
        Tester.checkEqual(p1.p2.P1P2Statics.NestedInterface.InnerInterface.s, (short) 5+22 );
        Tester.checkEqual(p1.p2.P1P2Statics.NestedInterface.InnerInterface.i,  5+22 );
        Tester.checkEqual(p1.p2.P1P2Statics.NestedInterface.InnerInterface.l,  5+22L);
        Tester.checkEqual(p1.p2.P1P2Statics.NestedInterface.InnerInterface.f, (float) 5+22 );
        Tester.checkEqual(p1.p2.P1P2Statics.NestedInterface.InnerInterface.d, (double) 5+22 );
        Tester.checkEqual(p1.p2.P1P2Statics.NestedInterface.InnerInterface.c, '5'+22);

        Tester.checkEqual(p1.p2.P1P2Statics.NestedInterface.InnerStaticInterfaceNoStatics.I, new Integer(6+22));
        Tester.checkEqual(p1.p2.P1P2Statics.NestedInterface.InnerStaticInterfaceNoStatics.b, (byte) 6+22 );
        Tester.checkEqual(p1.p2.P1P2Statics.NestedInterface.InnerStaticInterfaceNoStatics.s, (short) 6+22 );
        Tester.checkEqual(p1.p2.P1P2Statics.NestedInterface.InnerStaticInterfaceNoStatics.i,  6+22 );
        Tester.checkEqual(p1.p2.P1P2Statics.NestedInterface.InnerStaticInterfaceNoStatics.l,  6+22L);
        Tester.checkEqual(p1.p2.P1P2Statics.NestedInterface.InnerStaticInterfaceNoStatics.f, (float) 6+22 );
        Tester.checkEqual(p1.p2.P1P2Statics.NestedInterface.InnerStaticInterfaceNoStatics.d, (double) 6+22 );
        Tester.checkEqual(p1.p2.P1P2Statics.NestedInterface.InnerStaticInterfaceNoStatics.c, '6'+22);

        Tester.checkEqual(p1.p2.P1P2Statics.NestedInterface.InnerInterfaceNoStatics.I, new Integer(7+22));
        Tester.checkEqual(p1.p2.P1P2Statics.NestedInterface.InnerInterfaceNoStatics.b, (byte) 7+22 );
        Tester.checkEqual(p1.p2.P1P2Statics.NestedInterface.InnerInterfaceNoStatics.s, (short) 7+22 );
        Tester.checkEqual(p1.p2.P1P2Statics.NestedInterface.InnerInterfaceNoStatics.i,  7+22 );
        Tester.checkEqual(p1.p2.P1P2Statics.NestedInterface.InnerInterfaceNoStatics.l,  7+22L);
        Tester.checkEqual(p1.p2.P1P2Statics.NestedInterface.InnerInterfaceNoStatics.f, (float) 7+22 );
        Tester.checkEqual(p1.p2.P1P2Statics.NestedInterface.InnerInterfaceNoStatics.d, (double) 7+22 );
        Tester.checkEqual(p1.p2.P1P2Statics.NestedInterface.InnerInterfaceNoStatics.c, '7'+22);        
    }
}
