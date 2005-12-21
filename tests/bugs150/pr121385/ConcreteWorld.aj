public aspect ConcreteWorld extends World {
    pointcut greeting() :
    	execution(* Hello.sayWorld(..))
    	|| execution(* Hello.sayHello(..));
}
