
package model;

import java.lang.RuntimeException;

import base.ExceptionHandling;

public aspect ModelExceptionHandling extends ExceptionHandling {
    public pointcut scope() : within(*);

    protected RuntimeException convertCheckedException(Throwable t) {
        return new RuntimeException(t.getMessage(),t
}
