import foo.bar.Bax; //ERR: can't find type foo.bar.Bax
import a.b.c.*; //ERR: can't find package a.b.c

public class NotFound {
    public static void main(String[] args) {
        g().     //ERR: method g() not found
            bar();

        Mumble m = //ERR: type Mumble not found
            new Mumble(); //ERR: type Mumble not found

        m.go();

        Mumble m2 = null; //ERR: type Mumble not found

        CONST  //ERR: CONST not found
            .m(1);
    }
}
