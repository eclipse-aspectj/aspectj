public aspect RAj
{
    pointcut createR() : call(R.new()) && !within(RAj);
    Object around() : createR()
    {
        System.out.println("aspect running");
        return new R1();
    }
}
