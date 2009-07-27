public class Code {
}

interface Accessor<V>
{
    V get(String key);

    void set(String key, V value);
}

class AccessorImpl<V> implements Accessor<V> {

    public V get(String key) {
        System.out.println("Calling get(..)");
        return null;
    }

    public void set(String key, V value) {
        System.out.println("Calling set(..)");
    }

}

class Target {}

aspect TargetEnhancer {
    declare parents: Target extends AccessorImpl<String>;
}
