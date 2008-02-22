package q.r;

public @interface Fruity {
	Fruit value() default Fruit.BANANA;
}