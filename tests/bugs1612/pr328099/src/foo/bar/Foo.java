package foo.bar;

/**
 * Hello world!
 *
 */
public class Foo
{
    public void bar() {
        System.out.print(this.getClass().getSimpleName() + ".bar()");
    }

    public static void main( String[] args )
    {
        Foo foo = new Foo();
        foo.bar();
    }
}
