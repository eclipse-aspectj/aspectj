class Car {
  Car() {}
}

class FastCar extends Car {
  FastCar() {}
}

class Super {
  Car getCar() {
    return new Car();
  }
}

class Sub {
  FastCar getCar() {
    return new FastCar();
  }
}

public class CovBaseProgram02 {
  public static void main(String[] argv) {
    new CovBaseProgram02().run();
  }

  public void run() {
    Super instance_super = new Super();
    Sub   instance_sub   = new Sub();

    Car c1 = instance_super.getCar();
    FastCar c2 = instance_sub.getCar();
  }
}

// Lemon is a subclass of Car
// Sub is *not* a subclass of Super
