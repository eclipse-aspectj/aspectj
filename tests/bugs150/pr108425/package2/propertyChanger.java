package package2;

import java.lang.annotation.*;
import java.lang.*;

@Retention( RetentionPolicy.RUNTIME )
@Target({ ElementType.METHOD })
public @interface propertyChanger {
}