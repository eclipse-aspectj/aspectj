public abstract class BadGetPCD {
}

aspect A {
    pointcut foo(): get(void i)[];
}

