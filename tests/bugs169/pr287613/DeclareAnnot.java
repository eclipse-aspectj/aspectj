import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@interface Annot {}

aspect DeclareAnnot {
//    declare @method: * *.get*() || boolean *.is*(): @Annot;

//    declare @field: (String *.*) || (boolean *.*) : @Annot;
//    declare @field: (String *.*) && (boolean *.*) : @Annot;
//    declare @field: (String *.*) && !(boolean *.*) : @Annot;
    declare @field: ((String *.*) || !(String *.*)) && !(boolean *.*) : @Annot;
    declare @field: String *.* : @Annot;

/*
    declare @constructor: Person.new() || Person.new(*) : @Annot;

    declare @method: * *.get*() && boolean *.is*(): @Annot;

    declare @field: String *.* && boolean *.* : @Annot;

    declare @constructor: Person.new() && Person.new(*) : @Annot;

    declare @method: !(* *.get*()): @Annot;

    declare @field: !(String *.*) : @Annot;

    declare @constructor: !(Person.new()) : @Annot;
*/
}
