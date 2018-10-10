package de.scrum_master.aspect;

import java.util.List;
import java.util.stream.Collectors;

import java.util.function.Predicate;
import de.scrum_master.app.Apple;
import de.scrum_master.app.AppleController;

public privileged aspect AppleControllerITDAspect {
  public List<Apple> AppleController.namedApples(List<Apple> apples, String subString) {
    // Anonymous subclass works
    return apples.stream().filter(new Predicate<Apple>() {
      @Override
      public boolean test(Apple a) {
        return a.getType().contains(subString);
      }
    }).collect(Collectors.toList());
  }

  public List<Apple> AppleController.sweetApples(List<Apple> apples) {
    // Method reference works
    return apples.stream().filter(Apple::isSweet).collect(Collectors.toList());
  }

  public List<Apple> AppleController.sourApples(List<Apple> apples) {
    // Lambda causes IllegalAccessError
    return apples.stream().filter(a -> !a.isSweet()).collect(Collectors.toList());
  }
}
