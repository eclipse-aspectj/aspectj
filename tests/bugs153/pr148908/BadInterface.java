import java.util.Comparator;


public interface BadInterface {

    static final Comparator MY_COMPARATOR = new Comparator() {
        public int compare(Object o1, Object o2) {
            return 0;
        }
    };
}
