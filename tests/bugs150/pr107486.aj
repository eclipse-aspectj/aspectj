public class pr107486 {
    public Object f() {
        return new Object() {
            public String toString() {
                return "f";
            }
        };
    }
}