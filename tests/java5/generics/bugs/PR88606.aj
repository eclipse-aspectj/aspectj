
public class PR88606 {}

aspect Foo {

    private java.util.List<Foo> PR88606.list;
    
    private void bar() {
        java.util.List<Foo> li = new PR88606().list;
    }
}
