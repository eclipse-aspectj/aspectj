import java.util.ArrayList;
import java.util.List;

public aspect FooAspect {


        public <T> List<T> Foo.createList() {
                return new ArrayList<T>();
        }
}