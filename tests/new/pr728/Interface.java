import org.aspectj.testing.*;

/** @testcase PR#728 file order in type searching */
public interface Interface {
  static aspect Aspect {
    void aspectMethod( AnotherClass.InnerClass targ ) { 
        Tester.event( targ.toString());
    }
    before(AnotherClass.InnerClass targ) : target(targ)
        && !withincode(void Aspect.aspectMethod(AnotherClass.InnerClass))
        && call(public String toString()) {
        aspectMethod(targ);
    }
  }
}


