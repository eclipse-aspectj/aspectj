public aspect DeclareAnnotationsAspect {
  // These should be ignored, because @ToString has SOURCE retention
  declare @type : Application : @ToString;
  declare @method : * Application.*(..) : @ToString;
  declare @constructor : Application.new(..) : @ToString;
  declare @field : * Application.* : @ToString;

  // These should be applied, because @Marker has RUNTIME retention
  declare @type : Application : @Marker;
  declare @method : * Application.*(..) : @Marker;
  declare @constructor : Application.new(..) : @Marker;
  declare @field : * Application.* : @Marker;
}
