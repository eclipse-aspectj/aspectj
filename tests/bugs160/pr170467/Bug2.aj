import java.util.*;

// Trivial testcase to ensure the basics behave

abstract class BaseClass { }

aspect BaseClassAspect {
        public abstract void BaseClass.setSomething(Set somethings);
}

class ExtendedBaseClass extends BaseClass {
        @Override
        public void setSomething(Set somethings) { }
}


