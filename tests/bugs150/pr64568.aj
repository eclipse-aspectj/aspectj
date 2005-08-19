public aspect pr64568 {

	// itd that's trying to use a type pattern!
	private int foo.bar.*.aField;  // CE L4
	
}
