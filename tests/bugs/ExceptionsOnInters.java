// from Bug 29106 

public class ExceptionsOnInters {
  public static void main(String args[]) {
    try {
      ExceptionsOnInters.bomb();
    } catch (BombException e) {
      System.err.println(e);
    }
  }
}

aspect Bomb {
  public static void ExceptionsOnInters.bomb() throws BombException {
    throw new BombException("KABOOM");
  }
}

class BombException extends Exception {
  public BombException(String message) {
    super(message);
  }
}
