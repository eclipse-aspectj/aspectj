public class Test 
{
        public static void main(String[] args)
        {
                Foo<BInterface<Integer>> foo = new Foo<BInterface<Integer>>();

                foo.doSomething(null);
        }
}
