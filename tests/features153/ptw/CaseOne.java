// Types not in packages

class AClass {}
class BClass {}
class CClass {}

public aspect CaseOne pertypewithin(*Class) {
  public static void main(String []argv) {
    new Runner().run();
  }
}

class Runner {
  public void run() {
    if (CaseOne.hasAspect(AClass.class)) {
      System.out.println("AClass has an aspect instance");
      CaseOne instance = CaseOne.aspectOf(AClass.class);
      String name = instance.getWithinTypeName();
      System.out.println("The aspect instance thinks it is for type name "+name);
    }
  }
}
