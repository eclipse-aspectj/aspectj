
public aspect SampleAspect {
    before () : staticinitialization(!SampleAspect) {
        System.out.println("initializing class " + 
            thisJoinPointStaticPart.getSignature().getDeclaringType());
    }
}