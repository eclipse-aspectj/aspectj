package base;

public abstract aspect ExceptionHandling {
    public abstract pointcut scope();
    declare soft: Exception: scope();
}
