package p;

import java.util.ArrayList;

public class ErrorAspect {
    ArrayList arr = new ArrayList() {
        public boolean add(Object o) {
            doNothing();
            super.add(o);
        };
    }
}

