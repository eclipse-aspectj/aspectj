package a.b;

import c.d.DistantResource;
import e.f.SimpleAnnotation2;

// import the annotation type
aspect SimpleAspect4 {
  declare @type: DistantResource: @SimpleAnnotation2(classname="oranges");
}
