/**
 * AspectJ should have a rule that binding parameters lexically
 * within a ! is always an error.  These test some of the most
 * obvious forms of that.
 */
public aspect BindingInNotCf {
    pointcut pc1(Object o): this(o);

    pointcut pc2(Object o): !this(o); //CE

    pointcut pc3(Object o): !pc2(o); //CE

    pointcut pc4(Object o): !!pc1(o); //CE
}
