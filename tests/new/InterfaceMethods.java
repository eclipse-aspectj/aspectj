import org.aspectj.testing.Tester;

import java.lang.reflect.*;
import java.util.*;

public class InterfaceMethods {
    public static void main(String[] args) {
        new InterfaceMethods().realMain(args);
    }
    final static Object[] tuples = new Object[] {
        "clone", null, null,
        "equals", new Class[]{Object.class}, new Object[]{""},
        "finalize", null, null,
        "getClass", null, null,
        "hashCode", null, null,
        "notify", null, null,
        "notifyAll", null, null,
        "toString", null, null,
        "wait", null, null,
        "waitL", new Class[]{long.class}, new Object[]{new Long(3L)},
        "waitLI",  new Class[]{long.class, int.class}, new Object[]{new Long(4L), new Integer(5)},
    };
    final List list = new Vector();
    {
        for (int i = 0; i < tuples.length; i += 3) {
            List tuple = new Vector();
            tuple.add(tuples[i]+ "New");
            tuple.add(tuples[i+1] == null ? new Class[]{} : tuples[i+1]);
            tuple.add(tuples[i+2] == null ? new Object[]{} : tuples[i+2]);
            list.add(tuple);
        }
    }
    public void realMain(String[] argv) {
        Iterator iter = list.iterator();
        while (iter.hasNext()) {
            List tuple = (List) iter.next();
            String name = (String) tuple.get(0);
            Class[] params = (Class[]) tuple.get(1);
            Object[] args = (Object[]) tuple.get(2);
            boolean ran = false;
            Throwable caught = null;
            try {
                Object o = new SomeClass();
                o.getClass().getMethod(name, params).invoke(o, args);
                ran = true;
            } catch (Throwable t) {
                caught = t;
            } finally {
                Tester.check(ran, name + " didn't run" + (caught != null ? ":"+caught : ""));
            }
        }
    }    
}

interface SomeType {}
class SomeClass implements SomeType {
//      public Object cloneNew() { try { return clone(); } catch (Throwable t) {} return null; }
//      public boolean equalsNew(Object o) { return equals(o); }
//      public void finalizeNew() { try { finalize(); } catch (Throwable t) {} }
//      public Class getClassNew() { return getClass(); }
//      public int hashCodeNew() { return hashCode(); }
//      public void notifyNew() { try { notify(); } catch (Throwable t) {} }
//      public void notifyAllNew() { try { notifyAll(); } catch (Throwable t) {} }
//      public String toStringNew() { return toString(); }
//      public void waitNew() { try { wait(); } catch (Throwable t) {} }        
//      public void waitLNew(long l) { try { wait(l); } catch (Throwable t) {} }
//      public void waitLINew(long l, int i) { try { wait(l,i); } catch (Throwable t) {} }
}

aspect AspectToIntroduce_clone {
    introduction SomeType {
        public Object cloneNew() { try { return clone(); } catch (Throwable t) {} return null; }
    }
}

aspect AspectToIntroduce_equals {
    introduction SomeType {
        public boolean equalsNew(Object o) { return equals(o); }
    }
}

aspect AspectToIntroduce_finalize {
    introduction SomeType {
        public void finalizeNew() { try { finalize(); } catch (Throwable t) {} }
    }
}

aspect AspectToIntroduce_getClass {
    introduction SomeType {
        public Class getClassNew() { return getClass(); }
    }
}

aspect AspectToIntroduce_hashCode {
    introduction SomeType {
        public int hashCodeNew() { return hashCode(); }
    }
}

aspect AspectToIntroduce_notify {
    introduction SomeType {
        public void notifyNew() { try { notify(); } catch (Throwable t) {} }
    }
}

aspect AspectToIntroduce_notifyAll {
    introduction SomeType {
        public void notifyAllNew() { try { notifyAll(); } catch (Throwable t) {} }
    }
}

aspect AspectToIntroduce_toString {
    introduction SomeType {
        public String toStringNew() { return toString(); }
    }
}

aspect AspectToIntroduce_wait {
    introduction SomeType {
        public void waitNew() { try { wait(); } catch (Throwable t) {} }
    }
}

aspect AspectToIntroduce_waitL {
    introduction SomeType {
        public void waitLNew(long l) { try { wait(l); } catch (Throwable t) {} }
    }
}

aspect AspectToIntroduce_waitLI {
    introduction SomeType {
        public void waitLINew(long l, int i) { try { wait(l,i); } catch (Throwable t) {} }
    }
}
