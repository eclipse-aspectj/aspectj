package a.b;

// explicitly reference target type in another package
aspect SimpleAspect1 {
  declare @type: c.d.DistantResource: @SimpleAnnotation(classname="oranges");
}
