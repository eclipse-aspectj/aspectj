// Types not in packages - BClass won't get an aspect

class AClass {}
class BClass {}
class CClass {}

public aspect CaseThree pertypewithin(*Class && !BClass) {
  public static void main(String []argv) {
    new Runner().run();
  }
}

class Runner {
  public void run() {
    CaseThree aInstance = (CaseThree.hasAspect(AClass.class)?CaseThree.aspectOf(AClass.class):null);
    CaseThree bInstance = (CaseThree.hasAspect(BClass.class)?CaseThree.aspectOf(BClass.class):null);
    CaseThree cInstance = (CaseThree.hasAspect(CClass.class)?CaseThree.aspectOf(CClass.class):null);

    System.out.println("BClass aspect instance gives a within type name of "+
                       (bInstance==null?"<null>":bInstance.getWithinTypeName()));
    System.out.println("CClass aspect instance gives a within type name of "+
                       (cInstance==null?"<null>":cInstance.getWithinTypeName()));
    System.out.println("AClass aspect instance gives a within type name of "+
                       (aInstance==null?"<null>":aInstance.getWithinTypeName()));
  }
}
