public class Pair<F, S> {
    public Pair(F first, S second) { }
}

aspect IdempotentCache pertarget(cached()) {
    pointcut cached(): execution(public * *(..)) && within(Pair);
}
