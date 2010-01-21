// Interacting private ITDs

public class InteractingOldAndNew {
  public static void main(String []argv) {
    InteractingOldAndNew inst = new InteractingOldAndNew();
    inst.setI1(12);
    inst.setI2(65);
    int value = inst.getI1();
    if (value!=12) { throw new RuntimeException(Integer.toString(value)); }
    value = inst.getI2();
    if (value!=65) { throw new RuntimeException(Integer.toString(value)); }
  }
}


aspect X {
  private int InteractingOldAndNew.i;

  public int InteractingOldAndNew.getI1() {
    return i;
  }

  public void InteractingOldAndNew.setI1(int newvalue) {
    i = newvalue;
  }
}

aspect Y {
  private int InteractingOldAndNew.i;

  public int InteractingOldAndNew.getI2() {
    return i;
  }

  public void InteractingOldAndNew.setI2(int newvalue) {
    i = newvalue;
  }
}
