public aspect Pcds {
    public pointcut withinMe(): within(org.aspectj..*);
    public pointcut myTarget(): target(org.aspectj..*);
}
