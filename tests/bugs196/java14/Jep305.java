class Orange {
  public String name1 = "orange";
}

class Apple {
  public String name2 = "apple";
}

public class Jep305 {
  public static void main(String []argv) {
    print(new Orange());
    print(new Apple());
  }

  public static void print(Object obj) {
    if (obj instanceof Orange o) {
      System.out.println(o.name1);
    } else if (obj instanceof Apple a) {
      System.out.println(a.name2);
    }
  }
}
