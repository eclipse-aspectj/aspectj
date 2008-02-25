enum Color { RED, GREEN, AMBER }

@interface TrafficLight {
	Color value() default Color.RED; Color a() default Color.GREEN; Color c() default Color.GREEN; Color e() default Color.GREEN;
}

public class Parsing {
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
  pointcut p1(): execution(@TrafficLight(Color.GREEN) * *(..));
  pointcut p2(): execution(@TrafficLight(a=Color.GREEN) * *(..));
  pointcut p3(): execution(@TrafficLight(a=Color.RED,c=Color.RED) * *(..));
  pointcut p4(): execution(@TrafficLight(a=Color.RED,c=Color.RED,e=Color.RED) * *(..));
}
