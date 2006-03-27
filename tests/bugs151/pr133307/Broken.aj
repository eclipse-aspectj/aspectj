interface TestIF<T extends TestIF> {}

class TestClass {}

aspect TestAspect {
   declare parents: TestClass implements TestIF<TestClass>;
}