import org.aspectj.testing.*;

public class NullPointerOnGets {
    public static void main(String[] args) {
        Strings.add();
        C.go();
        Ints.add();
        Tester.checkAllEventsIgnoreDups();
    }
    
    static {
        Tester.expectEvent("static");
    }
}

class C {
    public String str0 = "str0";
    String str1 = "str1";
    protected String str2 = "str2";
    private String str3 = "str3";

    public static String sstr0 = "sstr0";
    static String sstr1 = "sstr1";
    protected static String sstr2 = "sstr2";
    private static String sstr3 = "sstr3";

    public int int0 = 1;
    int int1 = 2;
    protected int int2 = 3;
    private int int3 = 4;
    
    public static int sint0 = -1;
    static int sint1 = -2;
    protected static int sint2 = -3;
    private static int sint3 = -4;

    static void go() {
        Tester.event("static");
        C c = new C();
        eq(c.str0, "str0");   c.str0 = "str00";   eq(c.str0, "str00");
        eq(c.str1, "str1");   c.str1 = "str11";   eq(c.str1, "str11");
        eq(c.str2, "str2");   c.str2 = "str22";   eq(c.str2, "str22");
        eq(c.str3, "str3");   c.str3 = "str33";   eq(c.str3, "str33");
        
        eq(C.sstr0, "sstr0"); C.sstr0 = "sstr00"; eq(C.sstr0, "sstr00");
        eq(C.sstr1, "sstr1"); C.sstr1 = "sstr11"; eq(C.sstr1, "sstr11");
        eq(C.sstr2, "sstr2"); C.sstr2 = "sstr22"; eq(C.sstr2, "sstr22");
        eq(C.sstr3, "sstr3"); C.sstr3 = "sstr33"; eq(C.sstr3, "sstr33");

        eq(c.int0, 1); c.int0 = 100; eq(c.int0, 100);
        eq(c.int1, 2); c.int1 = 111; eq(c.int1, 111);
        eq(c.int2, 3); c.int2 = 122; eq(c.int2, 122);
        eq(c.int3, 4); c.int3 = 133; eq(c.int3, 133);
        
        eq(C.sint0, -1); C.sint0 = 200; eq(C.sint0, 200);
        eq(C.sint1, -2); C.sint1 = 211; eq(C.sint1, 211);
        eq(C.sint2, -3); C.sint2 = 222; eq(C.sint2, 222);
        eq(C.sint3, -4); C.sint3 = 233; eq(C.sint3, 233);
    }

    private static void eq(int    i0, int    i1) {Tester.checkEqual(i0,i1);}
    private static void eq(String s0, String s1) {Tester.checkEqual(s0,s1);}
}

aspect StringAspect {

    static void a(String s) { Tester.event(s); }

    //pointcuts
    pointcut gets_String_str0():   get(String C.str0);//[s];
    pointcut gets_String_str1():   get(String C.str1); //[s];
    pointcut gets_String_str2():   get(String C.str2); //[s];
    pointcut gets_String_str3():   get(String C.str3); //[s];
    pointcut gets_String_str0b():  get(String C.str0); //[];
    pointcut gets_String_str1b():  get(String C.str1); //[];
    pointcut gets_String_str2b():  get(String C.str2); //[];
    pointcut gets_String_str3b():  get(String C.str3); //[];
    pointcut sets_String_str0():   set(String C.str0); //[s][];
    pointcut sets_String_str1():   set(String C.str1); //[s][];
    pointcut sets_String_str2():   set(String C.str2); //[s][];
    pointcut sets_String_str3():   set(String C.str3); //[s][];
    pointcut sets_String_str0b():  set(String C.str0); //[][s];
    pointcut sets_String_str1b():  set(String C.str1); //[][s];
    pointcut sets_String_str2b():  set(String C.str2); //[][s];
    pointcut sets_String_str3b():  set(String C.str3); //[][s];
    pointcut sets_String_str0c():  set(String C.str0); //[o][s];
    pointcut sets_String_str1c():  set(String C.str1); //[o][s];
    pointcut sets_String_str2c():  set(String C.str2); //[o][s];
    pointcut sets_String_str3c():  set(String C.str3); //[o][s];
    pointcut gets_String_sstr0():  get(String C.str0); //[s];
    pointcut gets_String_sstr1():  get(String C.str1); //[s];
    pointcut gets_String_sstr2():  get(String C.str2); //[s];
    pointcut gets_String_sstr3():  get(String C.str3); //[s];
    pointcut gets_String_sstr0b(): get(String C.str0); //[];
    pointcut gets_String_sstr1b(): get(String C.str1); //[];
    pointcut gets_String_sstr2b(): get(String C.str2); //[];
    pointcut gets_String_sstr3b(): get(String C.str3); //[];
    pointcut sets_String_sstr0():  set(String C.str0); //[s][];
    pointcut sets_String_sstr1():  set(String C.str1); //[s][];
    pointcut sets_String_sstr2():  set(String C.str2); //[s][];
    pointcut sets_String_sstr3():  set(String C.str3); //[s][];  
    pointcut sets_String_sstr0b(): set(String C.str0); //[][s];
    pointcut sets_String_sstr1b(): set(String C.str1); //[][s];
    pointcut sets_String_sstr2b(): set(String C.str2); //[][s];
    pointcut sets_String_sstr3b(): set(String C.str3); //[][s];
    pointcut sets_String_sstr0c(): set(String C.str0); //[o][s];
    pointcut sets_String_sstr1c(): set(String C.str1); //[o][s];
    pointcut sets_String_sstr2c(): set(String C.str2); //[o][s];
    pointcut sets_String_sstr3c(): set(String C.str3); //[o][s];

    //befores
    before(): gets_String_str0()   { a("b gets_String_str0");   }
    before(): gets_String_str1()   { a("b gets_String_str1");   }
    before(): gets_String_str2()   { a("b gets_String_str2");   }
    before(): gets_String_str3()   { a("b gets_String_str3");   }
    before(): gets_String_str0b()  { a("b gets_String_str0b");  }
    before(): gets_String_str1b()  { a("b gets_String_str1b");  }
    before(): gets_String_str2b()  { a("b gets_String_str2b");  }
    before(): gets_String_str3b()  { a("b gets_String_str3b");  }
    before(): sets_String_str0()   { a("b sets_String_str0");   }
    before(): sets_String_str1()   { a("b sets_String_str1");   }
    before(): sets_String_str2()   { a("b sets_String_str2");   }
    before(): sets_String_str3()   { a("b sets_String_str3");   }
    before(): sets_String_str0b()  { a("b sets_String_str0b");  }
    before(): sets_String_str1b()  { a("b sets_String_str1b");  }
    before(): sets_String_str2b()  { a("b sets_String_str2b");  }
    before(): sets_String_str3b()  { a("b sets_String_str3b");  }
    before(): sets_String_str0c()  { a("b sets_String_str0c");  }
    before(): sets_String_str1c()  { a("b sets_String_str1c");  }
    before(): sets_String_str2c()  { a("b sets_String_str2c");  }
    before(): sets_String_str3c()  { a("b sets_String_str3c");  }
    before(): gets_String_sstr0()  { a("b gets_String_sstr0");  }
    before(): gets_String_sstr1()  { a("b gets_String_sstr1");  }
    before(): gets_String_sstr2()  { a("b gets_String_sstr2");  }
    before(): gets_String_sstr3()  { a("b gets_String_sstr3");  }
    before(): gets_String_sstr0b() { a("b gets_String_sstr0b"); }
    before(): gets_String_sstr1b() { a("b gets_String_sstr1b"); }
    before(): gets_String_sstr2b() { a("b gets_String_sstr2b"); }
    before(): gets_String_sstr3b() { a("b gets_String_sstr3b"); }
    before(): sets_String_sstr0()  { a("b sets_String_sstr0");  }
    before(): sets_String_sstr1()  { a("b sets_String_sstr1");  }
    before(): sets_String_sstr2()  { a("b sets_String_sstr2");  }
    before(): sets_String_sstr3()  { a("b sets_String_sstr3");  }
    before(): sets_String_sstr0b() { a("b sets_String_sstr0b"); }
    before(): sets_String_sstr1b() { a("b sets_String_sstr1b"); }
    before(): sets_String_sstr2b() { a("b sets_String_sstr2b"); }
    before(): sets_String_sstr3b() { a("b sets_String_sstr3b"); }
    before(): sets_String_sstr0c() { a("b sets_String_sstr0c"); }
    before(): sets_String_sstr1c() { a("b sets_String_sstr1c"); }
    before(): sets_String_sstr2c() { a("b sets_String_sstr2c"); }
    before(): sets_String_sstr3c() { a("b sets_String_sstr3c"); }
    
    //end-befores
    
    //arounds
    Object around(): gets_String_str0() { a("a gets_String_str0"); return proceed(); }
    Object around(): gets_String_str1() { a("a gets_String_str1"); return proceed(); }
    Object around(): gets_String_str2() { a("a gets_String_str2"); return proceed(); }
    Object around(): gets_String_str3() { a("a gets_String_str3"); return proceed(); }
    Object around(): gets_String_str0b() { a("a gets_String_str0b"); return proceed(); }
    Object around(): gets_String_str1b() { a("a gets_String_str1b"); return proceed(); }
    Object around(): gets_String_str2b() { a("a gets_String_str2b"); return proceed(); }
    Object around(): gets_String_str3b() { a("a gets_String_str3b"); return proceed(); }
    Object around(): sets_String_str0() { a("a sets_String_str0"); return proceed(); }
    Object around(): sets_String_str1() { a("a sets_String_str1"); return proceed(); }
    Object around(): sets_String_str2() { a("a sets_String_str2"); return proceed(); }
    Object around(): sets_String_str3() { a("a sets_String_str3"); return proceed(); }
    Object around(): sets_String_str0b() { a("a sets_String_str0b"); return proceed(); }
    Object around(): sets_String_str1b() { a("a sets_String_str1b"); return proceed(); }
    Object around(): sets_String_str2b() { a("a sets_String_str2b"); return proceed(); }
    Object around(): sets_String_str3b() { a("a sets_String_str3b"); return proceed(); }
    Object around(): sets_String_str0c() { a("a sets_String_str0c"); return proceed(); }
    Object around(): sets_String_str1c() { a("a sets_String_str1c"); return proceed(); }
    Object around(): sets_String_str2c() { a("a sets_String_str2c"); return proceed(); }
    Object around(): sets_String_str3c() { a("a sets_String_str3c"); return proceed(); }
    Object around(): gets_String_sstr0() { a("a gets_String_sstr0"); return proceed(); }
    Object around(): gets_String_sstr1() { a("a gets_String_sstr1"); return proceed(); }
    Object around(): gets_String_sstr2() { a("a gets_String_sstr2"); return proceed(); }
    Object around(): gets_String_sstr3() { a("a gets_String_sstr3"); return proceed(); }
    Object around(): gets_String_sstr0b() { a("a gets_String_sstr0b"); return proceed(); }
    Object around(): gets_String_sstr1b() { a("a gets_String_sstr1b"); return proceed(); }
    Object around(): gets_String_sstr2b() { a("a gets_String_sstr2b"); return proceed(); }
    Object around(): gets_String_sstr3b() { a("a gets_String_sstr3b"); return proceed(); }
    Object around(): sets_String_sstr0() { a("a sets_String_sstr0"); return proceed(); }
    Object around(): sets_String_sstr1() { a("a sets_String_sstr1"); return proceed(); }
    Object around(): sets_String_sstr2() { a("a sets_String_sstr2"); return proceed(); }
    Object around(): sets_String_sstr3() { a("a sets_String_sstr3"); return proceed(); }
    Object around(): sets_String_sstr0b() { a("a sets_String_sstr0b"); return proceed(); }
    Object around(): sets_String_sstr1b() { a("a sets_String_sstr1b"); return proceed(); }
    Object around(): sets_String_sstr2b() { a("a sets_String_sstr2b"); return proceed(); }
    Object around(): sets_String_sstr3b() { a("a sets_String_sstr3b"); return proceed(); }
    Object around(): sets_String_sstr0c() { a("a sets_String_sstr0c"); return proceed(); }
    Object around(): sets_String_sstr1c() { a("a sets_String_sstr1c"); return proceed(); }
    Object around(): sets_String_sstr2c() { a("a sets_String_sstr2c"); return proceed(); }
    Object around(): sets_String_sstr3c() { a("a sets_String_sstr3c"); return proceed(); }
    //end-arounds
    
    //afters
    after(): gets_String_str0()   { a("f gets_String_str0");   }
    after(): gets_String_str1()   { a("f gets_String_str1");   }
    after(): gets_String_str2()   { a("f gets_String_str2");   }
    after(): gets_String_str3()   { a("f gets_String_str3");   }
    after(): gets_String_str0b()  { a("f gets_String_str0b");  }
    after(): gets_String_str1b()  { a("f gets_String_str1b");  }
    after(): gets_String_str2b()  { a("f gets_String_str2b");  }
    after(): gets_String_str3b()  { a("f gets_String_str3b");  }
    after(): sets_String_str0()   { a("f sets_String_str0");   }
    after(): sets_String_str1()   { a("f sets_String_str1");   }
    after(): sets_String_str2()   { a("f sets_String_str2");   }
    after(): sets_String_str3()   { a("f sets_String_str3");   }
    after(): sets_String_str0b()  { a("f sets_String_str0b");  }
    after(): sets_String_str1b()  { a("f sets_String_str1b");  }
    after(): sets_String_str2b()  { a("f sets_String_str2b");  }
    after(): sets_String_str3b()  { a("f sets_String_str3b");  }
    after(): sets_String_str0c()  { a("f sets_String_str0c");  }
    after(): sets_String_str1c()  { a("f sets_String_str1c");  }
    after(): sets_String_str2c()  { a("f sets_String_str2c");  }
    after(): sets_String_str3c()  { a("f sets_String_str3c");  }
    after(): gets_String_sstr0()  { a("f gets_String_sstr0");  }
    after(): gets_String_sstr1()  { a("f gets_String_sstr1");  }
    after(): gets_String_sstr2()  { a("f gets_String_sstr2");  }
    after(): gets_String_sstr3()  { a("f gets_String_sstr3");  }
    after(): gets_String_sstr0b() { a("f gets_String_sstr0b"); }
    after(): gets_String_sstr1b() { a("f gets_String_sstr1b"); }
    after(): gets_String_sstr2b() { a("f gets_String_sstr2b"); }
    after(): gets_String_sstr3b() { a("f gets_String_sstr3b"); }
    after(): sets_String_sstr0()  { a("f sets_String_sstr0");  }
    after(): sets_String_sstr1()  { a("f sets_String_sstr1");  }
    after(): sets_String_sstr2()  { a("f sets_String_sstr2");  }
    after(): sets_String_sstr3()  { a("f sets_String_sstr3");  }
    after(): sets_String_sstr0b() { a("f sets_String_sstr0b"); }
    after(): sets_String_sstr1b() { a("f sets_String_sstr1b"); }
    after(): sets_String_sstr2b() { a("f sets_String_sstr2b"); }
    after(): sets_String_sstr3b() { a("f sets_String_sstr3b"); }
    after(): sets_String_sstr0c() { a("f sets_String_sstr0c"); }
    after(): sets_String_sstr1c() { a("f sets_String_sstr1c"); }
    after(): sets_String_sstr2c() { a("f sets_String_sstr2c"); }
    after(): sets_String_sstr3c() { a("f sets_String_sstr3c"); }
    //end-afters
}

class Strings {

    static void m(String s) { Tester.expectEvent(s); }
    static void add() {
        m("b gets_String_str0");   m("f gets_String_str0");   m("a gets_String_str0");
        m("b gets_String_str1");   m("f gets_String_str1");   m("a gets_String_str1");
        m("b gets_String_str2");   m("f gets_String_str2");   m("a gets_String_str2");
        m("b gets_String_str3");   m("f gets_String_str3");   m("a gets_String_str3");
        m("b gets_String_str0b");  m("f gets_String_str0b");  m("a gets_String_str0b");
        m("b gets_String_str1b");  m("f gets_String_str1b");  m("a gets_String_str1b");
        m("b gets_String_str2b");  m("f gets_String_str2b");  m("a gets_String_str2b");
        m("b gets_String_str3b");  m("f gets_String_str3b");  m("a gets_String_str3b");
        m("b sets_String_str0");   m("f sets_String_str0");   m("a sets_String_str0");
        m("b sets_String_str1");   m("f sets_String_str1");   m("a sets_String_str1");
        m("b sets_String_str2");   m("f sets_String_str2");   m("a sets_String_str2");
        m("b sets_String_str3");   m("f sets_String_str3");   m("a sets_String_str3");
        m("b sets_String_str0b");  m("f sets_String_str0b");  m("a sets_String_str0b");
        m("b sets_String_str1b");  m("f sets_String_str1b");  m("a sets_String_str1b");
        m("b sets_String_str2b");  m("f sets_String_str2b");  m("a sets_String_str2b");
        m("b sets_String_str3b");  m("f sets_String_str3b");  m("a sets_String_str3b");
        m("b sets_String_str0c");  m("f sets_String_str0c");  m("a sets_String_str0c");
        m("b sets_String_str1c");  m("f sets_String_str1c");  m("a sets_String_str1c");
        m("b sets_String_str2c");  m("f sets_String_str2c");  m("a sets_String_str2c");
        m("b sets_String_str3c");  m("f sets_String_str3c");  m("a sets_String_str3c");
        m("b gets_String_sstr0");  m("f gets_String_sstr0");  m("a gets_String_sstr0");
        m("b gets_String_sstr1");  m("f gets_String_sstr1");  m("a gets_String_sstr1");
        m("b gets_String_sstr2");  m("f gets_String_sstr2");  m("a gets_String_sstr2");
        m("b gets_String_sstr3");  m("f gets_String_sstr3");  m("a gets_String_sstr3");
        m("b gets_String_sstr0b"); m("f gets_String_sstr0b"); m("a gets_String_sstr0b");
        m("b gets_String_sstr1b"); m("f gets_String_sstr1b"); m("a gets_String_sstr1b");
        m("b gets_String_sstr2b"); m("f gets_String_sstr2b"); m("a gets_String_sstr2b");
        m("b gets_String_sstr3b"); m("f gets_String_sstr3b"); m("a gets_String_sstr3b");
        m("b sets_String_sstr0");  m("f sets_String_sstr0");  m("a sets_String_sstr0");
        m("b sets_String_sstr1");  m("f sets_String_sstr1");  m("a sets_String_sstr1");
        m("b sets_String_sstr2");  m("f sets_String_sstr2");  m("a sets_String_sstr2");
        m("b sets_String_sstr3");  m("f sets_String_sstr3");  m("a sets_String_sstr3");
        m("b sets_String_sstr0b"); m("f sets_String_sstr0b"); m("a sets_String_sstr0b");
        m("b sets_String_sstr1b"); m("f sets_String_sstr1b"); m("a sets_String_sstr1b");
        m("b sets_String_sstr2b"); m("f sets_String_sstr2b"); m("a sets_String_sstr2b");
        m("b sets_String_sstr3b"); m("f sets_String_sstr3b"); m("a sets_String_sstr3b");
        m("b sets_String_sstr0c"); m("f sets_String_sstr0c"); m("a sets_String_sstr0c");
        m("b sets_String_sstr1c"); m("f sets_String_sstr1c"); m("a sets_String_sstr1c");
        m("b sets_String_sstr2c"); m("f sets_String_sstr2c"); m("a sets_String_sstr2c");
        m("b sets_String_sstr3c"); m("f sets_String_sstr3c"); m("a sets_String_sstr3c");
    }
}


aspect IntAspect {

    static void a(String s) { Tester.event(s); }

    // start-non-static
    pointcut gets_int_int0():   get(int C.int0); //[s];
    pointcut gets_int_int1():   get(int C.int1); //[s];
    pointcut gets_int_int2():   get(int C.int2); //[s];
    pointcut gets_int_int3():   get(int C.int3); //[s];
    pointcut gets_int_int0b():  get(int C.int0); //[];
    pointcut gets_int_int1b():  get(int C.int1); //[];
    pointcut gets_int_int2b():  get(int C.int2); //[];
    pointcut gets_int_int3b():  get(int C.int3); //[];
    pointcut sets_int_int0():   set(int C.int0); //[s][];
    pointcut sets_int_int1():   set(int C.int1); //[s][];
    pointcut sets_int_int2():   set(int C.int2); //[s][];
    pointcut sets_int_int3():   set(int C.int3); //[s][];
    pointcut sets_int_int0b():  set(int C.int0); //[][s];
    pointcut sets_int_int1b():  set(int C.int1); //[][s];
    pointcut sets_int_int2b():  set(int C.int2); //[][s];
    pointcut sets_int_int3b():  set(int C.int3); //[][s];
    pointcut sets_int_int0c():  set(int C.int0); //[o][s];
    pointcut sets_int_int1c():  set(int C.int1); //[o][s];
    pointcut sets_int_int2c():  set(int C.int2); //[o][s];
    pointcut sets_int_int3c():  set(int C.int3); //[o][s];
    pointcut gets_int_sint0():  get(int C.int0); //[s];
    pointcut gets_int_sint1():  get(int C.int1); //[s];
    pointcut gets_int_sint2():  get(int C.int2); //[s];
    pointcut gets_int_sint3():  get(int C.int3); //[s];
    pointcut gets_int_sint0b(): get(int C.int0); //[];
    pointcut gets_int_sint1b(): get(int C.int1); //[];
    pointcut gets_int_sint2b(): get(int C.int2); //[];
    pointcut gets_int_sint3b(): get(int C.int3); //[];
    pointcut sets_int_sint0():  set(int C.int0); //[s][];
    pointcut sets_int_sint1():  set(int C.int1); //[s][];
    pointcut sets_int_sint2():  set(int C.int2); //[s][];
    pointcut sets_int_sint3():  set(int C.int3); //[s][];
    pointcut sets_int_sint0b(): set(int C.int0); //[][s];
    pointcut sets_int_sint1b(): set(int C.int1); //[][s];
    pointcut sets_int_sint2b(): set(int C.int2); //[][s];
    pointcut sets_int_sint3b(): set(int C.int3); //[][s];
    pointcut sets_int_sint0c(): set(int C.int0); //[o][s];
    pointcut sets_int_sint1c(): set(int C.int1); //[o][s];
    pointcut sets_int_sint2c(): set(int C.int2); //[o][s];
    pointcut sets_int_sint3c(): set(int C.int3); //[o][s];
    //end-pointcuts

    before(): gets_int_int0()   { a("b gets_int_int0");   }
    before(): gets_int_int1()   { a("b gets_int_int1");   }
    before(): gets_int_int2()   { a("b gets_int_int2");   }
    before(): gets_int_int3()   { a("b gets_int_int3");   }
    before(): gets_int_int0b()  { a("b gets_int_int0b");  }
    before(): gets_int_int1b()  { a("b gets_int_int1b");  }
    before(): gets_int_int2b()  { a("b gets_int_int2b");  }
    before(): gets_int_int3b()  { a("b gets_int_int3b");  }
    before(): sets_int_int0()   { a("b sets_int_int0");   }
    before(): sets_int_int1()   { a("b sets_int_int1");   }
    before(): sets_int_int2()   { a("b sets_int_int2");   }
    before(): sets_int_int3()   { a("b sets_int_int3");   }
    before(): sets_int_int0b()  { a("b sets_int_int0b");  }
    before(): sets_int_int1b()  { a("b sets_int_int1b");  }
    before(): sets_int_int2b()  { a("b sets_int_int2b");  }
    before(): sets_int_int3b()  { a("b sets_int_int3b");  }
    before(): sets_int_int0c()  { a("b sets_int_int0c");  }
    before(): sets_int_int1c()  { a("b sets_int_int1c");  }
    before(): sets_int_int2c()  { a("b sets_int_int2c");  }
    before(): sets_int_int3c()  { a("b sets_int_int3c");  }
    before(): gets_int_sint0()  { a("b gets_int_sint0");  }
    before(): gets_int_sint1()  { a("b gets_int_sint1");  }
    before(): gets_int_sint2()  { a("b gets_int_sint2");  }
    before(): gets_int_sint3()  { a("b gets_int_sint3");  }
    before(): gets_int_sint0b() { a("b gets_int_sint0b"); }
    before(): gets_int_sint1b() { a("b gets_int_sint1b"); }
    before(): gets_int_sint2b() { a("b gets_int_sint2b"); }
    before(): gets_int_sint3b() { a("b gets_int_sint3b"); }
    before(): sets_int_sint0()  { a("b sets_int_sint0");  }
    before(): sets_int_sint1()  { a("b sets_int_sint1");  }
    before(): sets_int_sint2()  { a("b sets_int_sint2");  }
    before(): sets_int_sint3()  { a("b sets_int_sint3");  }
    before(): sets_int_sint0b() { a("b sets_int_sint0b"); }
    before(): sets_int_sint1b() { a("b sets_int_sint1b"); }
    before(): sets_int_sint2b() { a("b sets_int_sint2b"); }
    before(): sets_int_sint3b() { a("b sets_int_sint3b"); }
    before(): sets_int_sint0c() { a("b sets_int_sint0c"); }
    before(): sets_int_sint1c() { a("b sets_int_sint1c"); }
    before(): sets_int_sint2c() { a("b sets_int_sint2c"); }
    before(): sets_int_sint3c() { a("b sets_int_sint3c"); }
    //end-befores

    Object around(): gets_int_int0()   { a("a gets_int_int0");   return proceed(); }
    Object around(): gets_int_int1()   { a("a gets_int_int1");   return proceed(); }
    Object around(): gets_int_int2()   { a("a gets_int_int2");   return proceed(); }
    Object around(): gets_int_int3()   { a("a gets_int_int3");   return proceed(); }
    Object around(): gets_int_int0b()  { a("a gets_int_int0b");  return proceed(); }
    Object around(): gets_int_int1b()  { a("a gets_int_int1b");  return proceed(); }
    Object around(): gets_int_int2b()  { a("a gets_int_int2b");  return proceed(); }
    Object around(): gets_int_int3b()  { a("a gets_int_int3b");  return proceed(); }
    Object around(): sets_int_int0()   { a("a sets_int_int0");   return proceed(); }
    Object around(): sets_int_int1()   { a("a sets_int_int1");   return proceed(); }
    Object around(): sets_int_int2()   { a("a sets_int_int2");   return proceed(); }
    Object around(): sets_int_int3()   { a("a sets_int_int3");   return proceed(); }
    Object around(): sets_int_int0b()  { a("a sets_int_int0b");  return proceed(); }
    Object around(): sets_int_int1b()  { a("a sets_int_int1b");  return proceed(); }
    Object around(): sets_int_int2b()  { a("a sets_int_int2b");  return proceed(); }
    Object around(): sets_int_int3b()  { a("a sets_int_int3b");  return proceed(); }
    Object around(): sets_int_int0c()  { a("a sets_int_int0c");  return proceed(); }
    Object around(): sets_int_int1c()  { a("a sets_int_int1c");  return proceed(); }
    Object around(): sets_int_int2c()  { a("a sets_int_int2c");  return proceed(); }
    Object around(): sets_int_int3c()  { a("a sets_int_int3c");  return proceed(); }
    Object around(): gets_int_sint0()  { a("a gets_int_sint0");  return proceed(); }
    Object around(): gets_int_sint1()  { a("a gets_int_sint1");  return proceed(); }
    Object around(): gets_int_sint2()  { a("a gets_int_sint2");  return proceed(); }
    Object around(): gets_int_sint3()  { a("a gets_int_sint3");  return proceed(); }
    Object around(): gets_int_sint0b() { a("a gets_int_sint0b"); return proceed(); }
    Object around(): gets_int_sint1b() { a("a gets_int_sint1b"); return proceed(); }
    Object around(): gets_int_sint2b() { a("a gets_int_sint2b"); return proceed(); }
    Object around(): gets_int_sint3b() { a("a gets_int_sint3b"); return proceed(); }
    Object around(): sets_int_sint0()  { a("a sets_int_sint0");  return proceed(); }
    Object around(): sets_int_sint1()  { a("a sets_int_sint1");  return proceed(); }
    Object around(): sets_int_sint2()  { a("a sets_int_sint2");  return proceed(); }
    Object around(): sets_int_sint3()  { a("a sets_int_sint3");  return proceed(); }
    Object around(): sets_int_sint0b() { a("a sets_int_sint0b"); return proceed(); }
    Object around(): sets_int_sint1b() { a("a sets_int_sint1b"); return proceed(); }
    Object around(): sets_int_sint2b() { a("a sets_int_sint2b"); return proceed(); }
    Object around(): sets_int_sint3b() { a("a sets_int_sint3b"); return proceed(); }
    Object around(): sets_int_sint0c() { a("a sets_int_sint0c"); return proceed(); }
    Object around(): sets_int_sint1c() { a("a sets_int_sint1c"); return proceed(); }
    Object around(): sets_int_sint2c() { a("a sets_int_sint2c"); return proceed(); }
    Object around(): sets_int_sint3c() { a("a sets_int_sint3c"); return proceed(); }
    //end-arounds

    after(): gets_int_int0()   { a("f gets_int_int0");   }
    after(): gets_int_int1()   { a("f gets_int_int1");   }
    after(): gets_int_int2()   { a("f gets_int_int2");   }
    after(): gets_int_int3()   { a("f gets_int_int3");   }
    after(): gets_int_int0b()  { a("f gets_int_int0b");  }
    after(): gets_int_int1b()  { a("f gets_int_int1b");  }
    after(): gets_int_int2b()  { a("f gets_int_int2b");  }
    after(): gets_int_int3b()  { a("f gets_int_int3b");  }
    after(): sets_int_int0()   { a("f sets_int_int0");   }
    after(): sets_int_int1()   { a("f sets_int_int1");   }
    after(): sets_int_int2()   { a("f sets_int_int2");   }
    after(): sets_int_int3()   { a("f sets_int_int3");   }
    after(): sets_int_int0b()  { a("f sets_int_int0b");  }
    after(): sets_int_int1b()  { a("f sets_int_int1b");  }
    after(): sets_int_int2b()  { a("f sets_int_int2b");  }
    after(): sets_int_int3b()  { a("f sets_int_int3b");  }
    after(): sets_int_int0c()  { a("f sets_int_int0c");  }
    after(): sets_int_int1c()  { a("f sets_int_int1c");  }
    after(): sets_int_int2c()  { a("f sets_int_int2c");  }
    after(): sets_int_int3c()  { a("f sets_int_int3c");  }
    after(): gets_int_sint0()  { a("f gets_int_sint0");  }
    after(): gets_int_sint1()  { a("f gets_int_sint1");  }
    after(): gets_int_sint2()  { a("f gets_int_sint2");  }
    after(): gets_int_sint3()  { a("f gets_int_sint3");  }
    after(): gets_int_sint0b() { a("f gets_int_sint0b"); }
    after(): gets_int_sint1b() { a("f gets_int_sint1b"); }
    after(): gets_int_sint2b() { a("f gets_int_sint2b"); }
    after(): gets_int_sint3b() { a("f gets_int_sint3b"); }
    after(): sets_int_sint0()  { a("f sets_int_sint0");  }
    after(): sets_int_sint1()  { a("f sets_int_sint1");  }
    after(): sets_int_sint2()  { a("f sets_int_sint2");  }
    after(): sets_int_sint3()  { a("f sets_int_sint3");  }
    after(): sets_int_sint0b() { a("f sets_int_sint0b"); }
    after(): sets_int_sint1b() { a("f sets_int_sint1b"); }
    after(): sets_int_sint2b() { a("f sets_int_sint2b"); }
    after(): sets_int_sint3b() { a("f sets_int_sint3b"); }
    after(): sets_int_sint0c() { a("f sets_int_sint0c"); }
    after(): sets_int_sint1c() { a("f sets_int_sint1c"); }
    after(): sets_int_sint2c() { a("f sets_int_sint2c"); }
    after(): sets_int_sint3c() { a("f sets_int_sint3c"); }
    //end-afters
}

class Ints {
    
    static void m(String s) { Tester.expectEvent(s); }
    static void add() {
        m("b gets_int_int0");   m("f gets_int_int0");   m("a gets_int_int0");
        m("b gets_int_int1");   m("f gets_int_int1");   m("a gets_int_int1");
        m("b gets_int_int2");   m("f gets_int_int2");   m("a gets_int_int2");
        m("b gets_int_int3");   m("f gets_int_int3");   m("a gets_int_int3");
        m("b gets_int_int0b");  m("f gets_int_int0b");  m("a gets_int_int0b");
        m("b gets_int_int1b");  m("f gets_int_int1b");  m("a gets_int_int1b");
        m("b gets_int_int2b");  m("f gets_int_int2b");  m("a gets_int_int2b");
        m("b gets_int_int3b");  m("f gets_int_int3b");  m("a gets_int_int3b");
        m("b sets_int_int0");   m("f sets_int_int0");   m("a sets_int_int0");
        m("b sets_int_int1");   m("f sets_int_int1");   m("a sets_int_int1");
        m("b sets_int_int2");   m("f sets_int_int2");   m("a sets_int_int2");
        m("b sets_int_int3");   m("f sets_int_int3");   m("a sets_int_int3");
        m("b sets_int_int0b");  m("f sets_int_int0b");  m("a sets_int_int0b");
        m("b sets_int_int1b");  m("f sets_int_int1b");  m("a sets_int_int1b");
        m("b sets_int_int2b");  m("f sets_int_int2b");  m("a sets_int_int2b");
        m("b sets_int_int3b");  m("f sets_int_int3b");  m("a sets_int_int3b");
        m("b sets_int_int0c");  m("f sets_int_int0c");  m("a sets_int_int0c");
        m("b sets_int_int1c");  m("f sets_int_int1c");  m("a sets_int_int1c");
        m("b sets_int_int2c");  m("f sets_int_int2c");  m("a sets_int_int2c");
        m("b sets_int_int3c");  m("f sets_int_int3c");  m("a sets_int_int3c");
        m("b gets_int_sint0");  m("f gets_int_sint0");  m("a gets_int_sint0");
        m("b gets_int_sint1");  m("f gets_int_sint1");  m("a gets_int_sint1");
        m("b gets_int_sint2");  m("f gets_int_sint2");  m("a gets_int_sint2");
        m("b gets_int_sint3");  m("f gets_int_sint3");  m("a gets_int_sint3");
        m("b gets_int_sint0b"); m("f gets_int_sint0b"); m("a gets_int_sint0b");
        m("b gets_int_sint1b"); m("f gets_int_sint1b"); m("a gets_int_sint1b");
        m("b gets_int_sint2b"); m("f gets_int_sint2b"); m("a gets_int_sint2b");
        m("b gets_int_sint3b"); m("f gets_int_sint3b"); m("a gets_int_sint3b");
        m("b sets_int_sint0");  m("f sets_int_sint0");  m("a sets_int_sint0");
        m("b sets_int_sint1");  m("f sets_int_sint1");  m("a sets_int_sint1");
        m("b sets_int_sint2");  m("f sets_int_sint2");  m("a sets_int_sint2");
        m("b sets_int_sint3");  m("f sets_int_sint3");  m("a sets_int_sint3");
        m("b sets_int_sint0b"); m("f sets_int_sint0b"); m("a sets_int_sint0b");
        m("b sets_int_sint1b"); m("f sets_int_sint1b"); m("a sets_int_sint1b");
        m("b sets_int_sint2b"); m("f sets_int_sint2b"); m("a sets_int_sint2b");
        m("b sets_int_sint3b"); m("f sets_int_sint3b"); m("a sets_int_sint3b");
        m("b sets_int_sint0c"); m("f sets_int_sint0c"); m("a sets_int_sint0c");
        m("b sets_int_sint1c"); m("f sets_int_sint1c"); m("a sets_int_sint1c");
        m("b sets_int_sint2c"); m("f sets_int_sint2c"); m("a sets_int_sint2c");
        m("b sets_int_sint3c"); m("f sets_int_sint3c"); m("a sets_int_sint3c");
    }
}
    
    
    
