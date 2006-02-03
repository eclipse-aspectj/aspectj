class EnumFun<E extends Enum<E>> {

  public E get() { return null; }

}

aspect SimpleAspect {

    Object around() : execution(* *(..)) {
        System.out.println("before");
        return proceed();
    }

}