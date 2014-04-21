public abstract aspect PerCFlowBug percflow(pointexp())
{
    String name = "bar";

    abstract pointcut pointexp();

    after() : pointexp()
    {
        System.out.println(name);
    }
}
