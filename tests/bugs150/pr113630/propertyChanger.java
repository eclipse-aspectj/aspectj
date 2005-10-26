package com.blueprint.util.aspectj5.test;

import java.lang.annotation.*;
import java.lang.*;

@Retention( RetentionPolicy.RUNTIME )
@Target({ ElementType.METHOD })
public @interface propertyChanger {
}

