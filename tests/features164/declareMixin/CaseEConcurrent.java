// TESTING: multiple instances causing factory invocation multiple times (but is cached correctly)
// Concurrency fix regression test for https://github.com/eclipse/org.aspectj/issues/198
import org.aspectj.lang.annotation.*;
import java.util.concurrent.atomic.AtomicInteger;

public class CaseEConcurrent {
  private String id;

  public static void main(String[]argv) throws InterruptedException {
    final CaseEConcurrent cea = new CaseEConcurrent("a");
    final CaseEConcurrent ceb = new CaseEConcurrent("b");

    Thread t1 = new Thread(new Runnable() { public void run() { ((I)cea).methodOne(); } });
    Thread t2 = new Thread(new Runnable() { public void run() { ((I)cea).methodTwo(); } });
    Thread t3 = new Thread(new Runnable() { public void run() { ((I)ceb).methodOne(); } });
    Thread t4 = new Thread(new Runnable() { public void run() { ((I)ceb).methodTwo(); } });

    t1.start();
    t2.start();
    t3.start();
    t4.start();

    t1.join();
    t2.join();
    t3.join();
    t4.join();
  }

  public CaseEConcurrent(String id) {
    this.id=id;
  }

  public String toString() {
    return "CaseEConcurrent instance: "+id;
  }

  // Helper methods 'doSomething' and 'callMe' help to produce byte code similar to what we need in order to fix
  // https://github.com/eclipse/org.aspectj/issues/198. If necessary, just temporarily uncomment, compile and analyse
  // the byte code, e.g. with JDK tool 'javap -v'.
  /*
  public void doSomething() {
    synchronized (this) {
      if (id == null)
        id = "doing something";
    }
    callMe(id);
  }

  public void callMe(String param) {
    System.out.println("I was called with param " + param);
  }
  */

}

aspect X {
  @DeclareMixin("CaseEConcurrent")
  public I createImplementation(Object o) {
    System.out.println("Delegate factory invoked for " + o);
    try { Thread.sleep(250); } catch (InterruptedException e) { throw new RuntimeException(e); }
    Implementation impl = new Implementation(o);
    return impl;
  }
}

interface I {
  void methodOne();
  void methodTwo();
}

class Implementation implements I {
  Object o;

  public Implementation(Object o) {
    this.o = o;
  }

  public void methodOne() {
    System.out.println("methodOne running on "+o);
  }
  public void methodTwo() {
    System.out.println("methodTwo running on "+o);
  }
}
