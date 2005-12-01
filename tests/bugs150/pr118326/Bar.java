public aspect Bar {
    public int Foo.x = null; // error
    
    public int Foo.y = 3;
    
    public int Foo.z = new Integer(42); // autoboxing
    
    public int Foo.i = "hello"; // error

}
