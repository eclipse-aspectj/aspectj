import org.aspectj.testing.Tester;
public class ClassAndInterface {
    public static void main(String[] args) {
        Tester.check(false, "shouldn't have compiled");
    }
}
class I {}
interface I {}
