
interface Common { }

interface Allergies extends Common { }

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
