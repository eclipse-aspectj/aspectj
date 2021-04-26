public abstract sealed class Person permits Employee, Manager {
  public void sayHello(String name) {
    System.out.println("Hello " + name);
  }
}
