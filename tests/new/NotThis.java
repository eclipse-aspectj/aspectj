import org.aspectj.testing.Tester;

/** From PR #496 from Mark Skipper 
 */

public class NotThis {

   public static void main(String[] args){
     new NotThis().go();
   }

   void go(){
      A a = new A(this);
      a.go();
      Tester.checkEqual(Q.buf.toString(), "foo:within(A):this(A):!within(B):!this(B):");
      Q.buf = new StringBuffer();
      B b = new B(this);
      b.go();
      Tester.checkEqual(Q.buf.toString(), "foo:");
   }

   public void foo(Object o){
       Q.buf.append("foo:");
   }
}

class A {
  NotThis t;

  A(NotThis n){ t = n; }

    void go(){ t.foo(this); }
}


class B{

  NotThis t;

  B(NotThis n){ t = n; }

  void go(){ t.foo(this); }

}


aspect Q {
    static StringBuffer buf = new StringBuffer();
    after(): call(void NotThis.foo(Object)) && within(A) {
        buf.append("within(A):");
    }
    after(): call(void NotThis.foo(Object)) && this(A) {
        buf.append("this(A):");
    }
    after(): call(void NotThis.foo(Object)) && !within(B) {
        buf.append("!within(B):");
    }
    after(): call(void NotThis.foo(Object)) && !this(B) {
        buf.append("!this(B):");
    }
}
