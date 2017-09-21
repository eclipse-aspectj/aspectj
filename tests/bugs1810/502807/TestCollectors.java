import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class TestCollectors {
    Set<Integer> ids;

    public TestCollectors(Set<Inner> inners) {
        ids = inners.stream().collect(Collectors.toList(Inner::getId));
//        ids = inners.stream().map(Inner::getId).collect(Collectors.toSet());
    } 

    public static void main() {
        Set<Inner> inners = new HashSet<>();
        inners.add(new Inner(1, "a"));
        inners.add(new Inner(1, "a"));

        new TestCollectors(inners);
    }


    public static class Inner {
        private int id;
        private String name;

        public Inner(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public int getId() { return id; }

        public String getName() { return name; }
    }
}

