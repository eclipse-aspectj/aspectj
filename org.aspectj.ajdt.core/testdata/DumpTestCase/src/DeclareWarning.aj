public aspect DeclareWarning {
	
	declare warning : Pointcuts.main() && within(HelloWorld) :
		"main()";
}
