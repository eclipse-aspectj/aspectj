package b;

public aspect Advises {

    declare @type: IsAdvised : @Deprecated;
    declare @method : IsAdvised.doNothing() : @Deprecated;
    declare @field : int IsAdvised.x : @Deprecated;
}
