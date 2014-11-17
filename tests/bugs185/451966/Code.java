
public class Code {
  public static void main(String []argv) {
    new Bar().foo();
  }
}
interface Common { }

interface Allergies extends Common { 
  default public void foo() {
  }
}

class Bar implements Allergies { }

aspect Y {
  private boolean Common.instancesInvariant() {
    return false;
  }
}

privileged aspect AspectJMLRac_allergies_Allergies {
  before(final Allergies object$rac): execution(* Allergies+.*(..)) && this(object$rac) {
    boolean b = object$rac.instancesInvariant();
  }
}
