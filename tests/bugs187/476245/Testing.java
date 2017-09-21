import java.util.Optional;

public class Testing {
    public static void main(String[] args) {
        Optional<Integer> value1 = Optional.of(15);
        Optional<Integer> value2 = Optional.of(30);
        boolean passed = value1.flatMap(v1 -> value2.map(v2 -> (v2 / v1) == 2)).orElse(false);
        System.out.println(passed);
    }
}
