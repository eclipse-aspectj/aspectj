package test.aop;

import java.io.Serializable;

public abstract class AbstractAdapter<T extends Serializable> {

    protected abstract T execute(T message);

}
