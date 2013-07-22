class Child extends Parent{

    public String mParent = "John";
    
    public Child(String parent) {
        this.mParent = parent;
    }
    
    public String getParent()
    {
        return this.mParent;
    }
}

class Parent {
    private String mName = "John";
    private int mAge = 50;
    
    public int getAge(){
        return mAge;
    }
}

aspect MyTest {

    public Child.new(String parent, int age) {
        this(parent);
        
        System.out.println("Get Age:" + super.getAge());
        System.out.println("Child Name:" + this.mParent);
    }
}

public class Code {
  public static void main(String []argv) {
    new Child("Andy",5);
  }
}
