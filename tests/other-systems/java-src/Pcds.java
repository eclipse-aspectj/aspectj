public aspect Pcds {
    public pointcut withinMe(): within(java..*) || within(javax..*);
    public pointcut myTarget(): target(java..*) || target(javax..*);
}
