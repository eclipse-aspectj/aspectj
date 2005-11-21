// random collection of pointcuts to check that
// reflective world and PointcutParser can interpret
// them correctly.
import java.lang.annotation.*;

public aspect PointcutLibrary {
	
	public pointcut propertyAccess() : get(* *);
	public pointcut propertyUpdate() : set(* *);
	public pointcut methodExecution() : execution(* *(..));
	public pointcut propertyGet() : execution(!void get*(..));
	public pointcut propertySet(Object newValue) 
		: execution(void set*(..)) && args(newValue);
	public pointcut getAndThis(Object thisObj) :
		get(* *) && this(thisObj);
	public pointcut getAndTarget(Object targetObj) :
		get(* *) && target(targetObj);
	public pointcut getAndAtAnnotation(MyAnn ann) :
		get(* *) && @annotation(ann);
	public pointcut getAndAtWithin(MyAnn ann) :
		get(* *) && @within(ann);
	public pointcut getAndAtWithinCode(MyAnn ann) :
		get(* *) && @withincode(ann);
	public pointcut getAndAtThis(MyAnn ann) :
		get(* *) && @this(ann);
	public pointcut getAndAtTarget(MyAnn ann) :
		get(* *) && @target(ann);
	public pointcut setAndAtArgs(MyAnn ann) :
		set(* *) && @args(ann);
	
	
}

@Retention(RetentionPolicy.RUNTIME)
@interface MyAnn {}