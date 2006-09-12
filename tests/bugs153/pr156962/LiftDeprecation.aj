public aspect LiftDeprecation {
    declare @type: hasmethod(@Deprecated * *(..)): @Deprecated;
}
