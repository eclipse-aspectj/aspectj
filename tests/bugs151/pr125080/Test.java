import java.util.*;

interface AspectInterface<T, S> { }

abstract aspect AbstractAspect<T> implements AspectInterface<T, Integer> {}	

aspect ConcreteAspect extends AbstractAspect<String> { 

  public static void main(String []argv) {
  }
}
