import org.aspectj.testing.Tester;
import java.util.*;


public class MethodSigs {
    public static void main(String[] args) {
        new MethodSigs().realMain(args);
    }

    String want;
    void want(String want) { this.want = want; }
    void w(String s) { want(s); }
    void have(Object have, Object msg) { Tester.checkEqual(want, have, msg+""); }
    void have(Object have) { Tester.checkEqual(want, have); }
    
    public void realMain(String[] args) {
        lists();
        integers();
    }
        
    void lists() {
        Object o     = new Object() { public String toString() { return "o"; } };
        Object o1    = new Object() { public String toString() { return "o"; } };
        Object o2    = new Object() { public String toString() { return "o"; } };
        List l = new Vector() { public String toString()  { return "l:"+super.toString(); } };
        List l1 = new Vector() { public String toString() { return "l1:"+super.toString(); } };
        List l2 = new Vector() { public String toString() { return "l2:"+super.toString(); } };
        Collection c = new Vector() { public String toString()  { return "c:"+super.toString(); } };
        Collection c1 = new Vector() { public String toString() { return "c1:"+super.toString(); } };
        Collection c2 = new Vector() { public String toString() { return "c2:"+super.toString(); } };
        Set s = new HashSet() { public String toString()  { return "s:"+super.toString(); } };
        Set s1 = new HashSet() { public String toString() { return "s1:"+super.toString(); } };
        Set s2 = new HashSet() { public String toString() { return "s2:"+super.toString(); } };

        want("a:Object,Object");         a(o1,o2);
        want("a:List,Object");           a(l,o);
        want("a:Object,List");           a(o,l);
        want("a:Collection,Object");     a(c,o);
        want("a:Object,Collection");     a(o,c);
        want("a:List,Collection");       a(l,c);
        want("a:Collection,List");       a(c,l);
        want("a:Collection,Collection"); a(c1,c2);
        want("a:Set,Collection");        a(s,c);
        want("a:Collection,Set");        a(c,s);
        want("a:Set,Set");               a(s1,s2);
        want("a:List,Set");              a(l,s);
        want("a:Set,List");              a(s,l);
    }

    public void a(Object o1, Object o2)         { have("a:Object,Object"); }
    public void a(List l, Object o)             { have("a:List,Object"); }
    public void a(Object o, List l)             { have("a:Object,List"); }
    public void a(Collection c, Object o)       { have("a:Collection,Object"); }
    public void a(Object o, Collection c)       { have("a:Object,Collection"); }
    public void a(List l, Collection c)         { have("a:List,Collection"); }
    public void a(Collection c,List  l)         { have("a:Collection,List"); }
    public void a(Collection c1, Collection c2) { have("a:Collection,Collection"); }
    public void a(Set s, Collection c)          { have("a:Set,Collection"); }
    public void a(Collection c,Set s)           { have("a:Collection,Set"); }
    public void a(Set s1, Set s2)               { have("a:Set,Set"); }
    public void a(List l, Set s)                { have("a:List,Set"); }
    public void a(Set s,List l)                 { have("a:Set,List"); }

    void integers() {
        Integer i   = new Integer(0);
        Integer i1   = new Integer(1);
        Integer i2   = new Integer(2);
        Object o     = new Object() { public String toString() { return "o"; } };
        Object o1    = new Object() { public String toString() { return "o"; } };
        Object o2    = new Object() { public String toString() { return "o"; } };
        Object oi    = new Integer(3);
        
        w("Object,Object");   f(o1,o2);
        w("Integer,Object");  f(i,o);
        w("Object,Integer");  f(o,i);
        w("Integer,Integer"); f(i1,i2);
        w("Object,Object");   f(oi,oi);
        w("Object,Object");   f(oi,o);
        w("Object,Object");   f(o,oi);
    }

    public void f(Object o1, Object o2)   { have("Object,Object",   o1+":"+o2);  }
    public void f(Integer i, Object o)    { have("Integer,Object",  i+":"+o);    }
    public void f(Object o, Integer i)    { have("Object,Integer",  o+":"+i);    }
    public void f(Integer i1, Integer i2) { have("Integer,Integer", i1+":"+i2);  }
}
