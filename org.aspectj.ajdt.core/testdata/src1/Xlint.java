public aspect Xlint {
	pointcut foo(): this(NotFoundType);
}