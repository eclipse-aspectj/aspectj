class Bug_Provider {
  public void enable_bug(Object argument) {}
}

class Bug_Checker<T> extends Bug_Provider {
  public T is_bug_enabled() {
    return (T) new Boolean(true);
  }
}

public class pr108104 {
  public static void main(String[] args) throws InterruptedException {
    final Bug_Checker<Boolean> first = new Bug_Checker<Boolean>() {
      @Override // compiler agrees, this is an override
      public Boolean is_bug_enabled() {
        return new Boolean(false);
      }
    };
    System.out.println("is bug enabled? " + first.is_bug_enabled()); // false

    first.enable_bug(null);
 
    final Bug_Checker<Boolean> second = new Bug_Checker<Boolean>() {
      @Override 
      public Boolean is_bug_enabled() {
        return new Boolean(false);
      }
    };
    System.out.println("is bug enabled? " +second.is_bug_enabled()); // true!
  }
}