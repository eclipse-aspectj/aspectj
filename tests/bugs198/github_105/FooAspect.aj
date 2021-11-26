public aspect FooAspect {
  declare @type:(@FooAnnotation *) :
    @BarAnnotation(name = "from FooAspect");
}
