package other.module;

import java.util.ArrayList;
import java.util.List;

import my.module.Modular;

public class Application {
  List<String> list = new ArrayList<>();
  Modular modular = new Modular();

  public static void main(String[] args) {
    System.out.println("One modular class can use another one");
  }
}
