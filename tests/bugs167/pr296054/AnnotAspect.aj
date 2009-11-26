@SuppressWarnings("nls")
public aspect AnnotAspect {
    declare @field : * AnnotDemo.* : @Demo(myValues={"alfa", "beta", "gamma"});

}
class AnnotDemo {
//  that works fine in the java class 
//@Demo(myValues={"alfa", "beta", "gamma"})
private int annotateMe;
}  
