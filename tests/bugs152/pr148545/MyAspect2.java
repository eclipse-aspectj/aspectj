import java.util.Arrays;

privileged public aspect MyAspect2 {

        Object around(MyClass o, MyAnnotation a) :
                        execution(@MyAnnotation * *(..)) &&
                        target(o) &&
                        @annotation(a) {
                if (isOneOf(o.getValue(), a.value())==null)
                        throw new IllegalStateException(
                                        o.getValue() +
                                        " is not one of " +
                                        Arrays.toString(a.value()));
                return proceed(o, a);
        }

        private static final <T> T isOneOf(T obj, T[] arr) {
                for (T el : arr) if (obj == el) return obj;
                return null;
        }
}