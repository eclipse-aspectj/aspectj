class Bug_Provider {
 public void enable_bug(Object argument) {}
}

class Null_Provider<T> extends Bug_Provider {
 public T get_typed_null() {
   return null;
 }
}

public class pr107898 {
 public static void main(String[] args) {
   Null_Provider<Integer> null_provider = new Null_Provider<Integer>() {};
   null_provider.enable_bug(null);
   Integer i = null_provider.get_typed_null(); // type mismatch
 }
}