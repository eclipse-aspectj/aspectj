package test135;

public aspect JoinPointFields extends test135.pack.JoinPointFields issingleton() { //of eachJVM() {
    protected pointcut onTypes(): target(*);

    private int x = protectedField;
}
