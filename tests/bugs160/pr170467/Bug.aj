import java.util.*;

// this should be OK, the parameterized forms of Set are the same

abstract class BaseClass { }

aspect BaseClassAspect {
        public abstract void BaseClass.setSomething(Set<String> somethings);
}

class ExtendedBaseClass extends BaseClass {
        @Override
        public void setSomething(Set<String> somethings) { }
}


