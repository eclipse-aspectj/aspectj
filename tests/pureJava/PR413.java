public class PR413 {
    public static void main(String[] args) {
        new PR413().realMain(args);
    }
    public void realMain(String[] args) {
    }
    
    public PR413() {
    }
}

interface Interface {
  public static class InnerClass {}

    public static int field = 2;
}
abstract class Abstract {
  public static class InnerClass {}

    public static double field = 3.14;
}
abstract class AbstractConflictingClass
    extends Abstract 
    implements Interface
{
}

class NonAbstractConflictingClass
    extends Abstract 
    implements Interface
{
}
class NonAbstractClass implements Interface {
    public static class InnerClass {}
}
class NonAbstractExtendingClass extends Abstract {
    public static class InnerClass {}
}
abstract class AbstractConflictingClassThatRedefinesInnerClass
    extends Abstract 
    implements Interface
{
    public static class InnerClass {}
}

class NonAbstractConflictingClassThatRedefinesInnerClass
    extends Abstract 
    implements Interface               
{
    public static class InnerClass {}
}

