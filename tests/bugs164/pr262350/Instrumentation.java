package dImmunix;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
public aspect Instrumentation {
        after(Object l) throwing: lock() && args(l) {
        }
}

