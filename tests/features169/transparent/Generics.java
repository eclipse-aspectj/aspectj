import java.util.*;

public class Generics<X> {
  public static void main(String []argv) {
    Generics<String> inst = new Generics<String>();
    List<String> ls = new ArrayList<String>();
    inst.setter(ls);
    List<String> newls = inst.getter();

    inst.setThing("abc");
    String s = inst.getThing();
  }
}

aspect X {
  private List<String> Generics.listOfString;
 
  public List<String> Generics.getter() {
    return listOfString;
  }

  public void Generics.setter(List<String> los) {
    listOfString = los;
  }
}

aspect Y {
  private T Generics<T>.thing;

  public T Generics<T>.getThing() {
    return thing;
  }

  public void Generics<T>.setThing(T thing) {
    this.thing = thing;
  }
}
