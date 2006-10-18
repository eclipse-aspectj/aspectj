// Types not in packages, and multiple mixed up instances

class AClass {}
class BClass {}
class CClass {}

public aspect CaseTwo pertypewithin(*Class) {
  public static void main(String []argv) {
    new Runner().run();
  }
}

class Runner {
  public void run() {
    CaseTwo aInstance = (CaseTwo.hasAspect(AClass.class)?CaseTwo.aspectOf(AClass.class):null);
    CaseTwo bInstance = (CaseTwo.hasAspect(BClass.class)?CaseTwo.aspectOf(BClass.class):null);
    CaseTwo cInstance = (CaseTwo.hasAspect(CClass.class)?CaseTwo.aspectOf(CClass.class):null);

    System.out.println("BClass aspect instance gives a within type name of "+
                       (bInstance==null?"<null>":bInstance.getWithinTypeName()));
    System.out.println("CClass aspect instance gives a within type name of "+
                       (cInstance==null?"<null>":cInstance.getWithinTypeName()));
    System.out.println("AClass aspect instance gives a within type name of "+
                       (aInstance==null?"<null>":aInstance.getWithinTypeName()));
  }
}
