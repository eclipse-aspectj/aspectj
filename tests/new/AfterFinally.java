import org.aspectj.testing.Tester;

import java.util.*;

public class AfterFinally {
    public static void main(String[] args) {
        new AfterFinally().m("hi");
    }

    private String getX() { return "X"; }

    public Collection m(String key) {
        String x = null;
        ArrayList y = new ArrayList();
        Iterator i = null;
        try {
            x = this.getX();
            Collection personList = new ArrayList();
            
            y.add("foo");
            //prepStmt.setString(1, name);
            i = y.iterator();
            
            while (i.hasNext()) {
                personList.add(new String(i.next() + " foo"));
            }
            return personList;
        } catch (Exception e) {
            throw new RuntimeException("bad:" + e);
        } finally {
            x.toString();
            y.toString();
            i.toString();
        }
    }
}


aspect A {
    before(): execution(* *(..)) && within(AfterFinally) {
        System.out.println(thisJoinPoint);
    }
    after(): execution(* *(..)) && within(AfterFinally) {
        System.out.println(thisJoinPoint);
    }
}
