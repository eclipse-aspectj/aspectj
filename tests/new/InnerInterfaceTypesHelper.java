
/** @testcase PR#685 subaspect method declaration on superaspect inner interface (types) */
aspect ConcreteAspect extends AbstractAspect {
    /** bug iff method declaration on parent inner interface */
    public Object InnerInterface.getThis() {
        return new Object(); 
    } 
}
