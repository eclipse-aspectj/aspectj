public aspect WarnDeprecatedMethod {
    public pointcut execDepr(): execution(@Deprecated * *(..));
    declare warning: execDepr(): "deprecated method";	
}
