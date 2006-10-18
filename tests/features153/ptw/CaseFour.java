// Types in packages

package a.b;

class AClass {}
class BClass {}
class CClass {}

public aspect CaseFour pertypewithin(*Class) {
  public static void main(String []argv) {
    new Runner().run();
  }
}

class Runner {
  public void run() {
    CaseFour aInstance = (CaseFour.hasAspect(AClass.class)?CaseFour.aspectOf(AClass.class):null);
    CaseFour bInstance = (CaseFour.hasAspect(BClass.class)?CaseFour.aspectOf(BClass.class):null);
    CaseFour cInstance = (CaseFour.hasAspect(CClass.class)?CaseFour.aspectOf(CClass.class):null);

    System.out.println("BClass aspect instance gives a within type name of "+
                       (bInstance==null?"<null>":bInstance.getWithinTypeName()));
    System.out.println("CClass aspect instance gives a within type name of "+
                       (cInstance==null?"<null>":cInstance.getWithinTypeName()));
    System.out.println("AClass aspect instance gives a within type name of "+
                       (aInstance==null?"<null>":aInstance.getWithinTypeName()));
  }
}
