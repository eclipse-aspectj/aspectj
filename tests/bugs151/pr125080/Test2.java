import java.util.*;

abstract aspect AbstractAspect<T> implements AspectInterface<T, Integer> {}	

interface AspectInterface<T, S extends Number> { }

aspect ConcreteAspect extends AbstractAspect<Student> { 
  public static void main(String []argv) {
  }
}

class Student {
    private String name;
    
    public Student(String n) {
	name = n;
    }
    
    public String toString() { return name; }
}
