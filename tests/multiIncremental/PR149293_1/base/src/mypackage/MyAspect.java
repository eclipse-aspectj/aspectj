package mypackage;

privileged public aspect MyAspect {

	declare @type : MyInterface+ : @MyBaseClass.MyAspectPresent;

}
