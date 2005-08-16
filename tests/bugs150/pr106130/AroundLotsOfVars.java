public aspect AroundLotsOfVars {

    Object around() : execution(* main(..)) {
	System.out.println("hello");
        return proceed();
    }


}
