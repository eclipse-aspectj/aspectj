package com.foo;

import java.lang.annotation.*;

@Target({ElementType.ANNOTATION_TYPE,ElementType.LOCAL_VARIABLE,ElementType.METHOD,ElementType.PACKAGE,ElementType.PARAMETER,ElementType.CONSTRUCTOR})
public @interface Anno6 {
}
