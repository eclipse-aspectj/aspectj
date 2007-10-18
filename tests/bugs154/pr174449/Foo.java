abstract aspect Replicate<T> {

  protected pointcut broadcast(T servant);

  void around(T servant): broadcast(servant) {
    System.err.println("around advice executing: servant class is "+servant.getClass());
    proceed(servant);
  }

}

aspect ReplicateConcreteB extends Replicate<Boo> {
  protected pointcut broadcast(Boo servant) : call(* *.setScene(..)) && target(servant);
}

aspect ReplicateConcreteG extends Replicate<Goo> {
  protected pointcut broadcast(Goo servant) : call(* *.setScene(..)) && target(servant);
}

public class Foo {
  public static void main(String []argv) {
    new Boo().setScene();
    new Goo().setScene();
  }
}

class Boo {
  public void setScene() {}
}

class Goo {
  public void setScene() {}
}
