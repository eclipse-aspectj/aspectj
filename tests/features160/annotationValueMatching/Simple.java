enum Color { RED, GREEN, AMBER }

@interface TrafficLight {
	Color value() default Color.RED;
}

public class Simple {
	public static void main(String[] args) {
		
	}
}

class Marked {

  public void a() {}

  @TrafficLight
  public void b() {}

  @TrafficLight(Color.RED)
  public void c() {}

  @TrafficLight(Color.GREEN)
  public void d() {}
}

aspect X {
  before(): execution(@TrafficLight(Color.RED) * *(..)) {}
}