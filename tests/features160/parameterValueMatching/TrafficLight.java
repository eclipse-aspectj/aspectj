package p;

public @interface TrafficLight {
	Color value() default Color.RED;
}