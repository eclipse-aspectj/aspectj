package de.scrum_master.app;

import java.util.Arrays;
import java.util.List;

public class AppleController {
  private static final List<Apple> APPLES =
    Arrays.asList(new Apple("Granny Smith", false), new Apple("Golden Delicious", true));

  public static void main(String[] args) {
    AppleController appleController = new AppleController();
    System.out.println("Named: " + appleController.namedApples(APPLES, "Smith"));
    System.out.println("Sweet: " + appleController.sweetApples(APPLES));
    System.out.println("Sour:  " + appleController.sourApples(APPLES));
  }
}
