public class PropertySupport<T extends PropertySupport<T>> {

  public static void main(String []argv) {
  }
}


class Two {
}

aspect X {
  declare parents: Two extends PropertySupport;
}

