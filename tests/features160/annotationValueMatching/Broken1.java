enum Color { RED, GREEN, AMBER }

@interface TrafficLight {
	Color value() default Color.RED;
}

public class Broken1 {
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
  pointcut p1(): execution(@TrafficLight(a) * *(..)); // value of just 'a' doesn't mean anything - only enums supported right now, let's say 'invalid annotation value'
}
