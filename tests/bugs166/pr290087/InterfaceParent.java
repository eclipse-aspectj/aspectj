public class InterfaceParent<T extends Interface> extends GenericParent<T> {
  public InterfaceParent(Class<? extends T> c) {
    super(c);
  }
}

