import java.lang.annotation.*;

public aspect RetentionTime {
	
	pointcut withinType() : @within(Foo);
	pointcut withinTypeBind(Foo foo) : @within(foo);
	pointcut withinTypeClass() : @within(Goo);
	pointcut withinTypeClassBind(Goo goo) : @within(goo);
	
	pointcut withincodeAnn() : @withincode(Foo);
	pointcut withincodeAnnBind(Foo foo) : @withincode(foo);
	pointcut withincodeAnnClass() : @withincode(Goo);
	pointcut withincodeAnnBindClass(Goo goo) : @withincode(goo);
	
	pointcut atann() : @annotation(Foo);
	pointcut atannBind(Foo foo) : @annotation(foo);
	pointcut atannClass() : @annotation(Goo);
	pointcut atannBindClass(Goo goo) : @annotation(goo);
		
}

@Retention(RetentionPolicy.RUNTIME) @interface Foo {}

@interface Goo {}