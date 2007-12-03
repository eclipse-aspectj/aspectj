package a.b;

import c.d.DistantResource;

// import target type
aspect SimpleAspect1 {
  declare @type: DistantResource: @SimpleAnnotation(classname="oranges");
}
