package a.b.c;

import java.lang.annotation.*;
import x.y.z.Level;

@Retention(RetentionPolicy.RUNTIME)
public @interface Anno { Level value();}
  
