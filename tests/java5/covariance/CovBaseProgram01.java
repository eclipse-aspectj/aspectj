class Car {}

class FastCar extends Car {}

class Super {
  Car getCar() {
    return new Car();
  }
}

class Sub extends Super {
  FastCar getCar() {
    return new FastCar();
  }
}

public class CovBaseProgram01 {
  public static void main(String[] argv) {
    new CovBaseProgram01().run();
  }

  public void run() {
    Super instance_super = new Super();
    Sub   instance_sub   = new Sub();

    Car c1 = instance_super.getCar();
    Car c2 = instance_sub.getCar();
  }
}

// FastCar is a subclass of Car.
// Sub is a subclass of Super.