public aspect DeclareError {
	
	declare error : Pointcuts.main() && within(HelloWorld) :
		"main()";

}
