package test135.pack;

public aspect PackJoinPointFields
    extends JoinPointFields issingleton() { /*of eachJVM() {*/
    protected pointcut onTypes(): target(test135.pack.PackFoo);
}
