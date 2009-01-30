public aspect A {

pointcut cf(): execution(* *(..)) && !cflow(cf());

before(): cf() {}
}
