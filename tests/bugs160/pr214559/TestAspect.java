aspect TestAspect  {
        declare parents: 
                TestClass implements Interface1;

        declare parents: 
                TestClass && Interface1+ implements Interface1TestClass;
}
