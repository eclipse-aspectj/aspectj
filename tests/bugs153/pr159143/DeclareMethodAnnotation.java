// Preserve lineation of affected types or redo expected messages



@interface MtAn {} 




public aspect DeclareMethodAnnotation {

    

    // ------------------ affected types
    static class Untyped {
        void untypedName() {} // declare warning 16
        void untypedPrefix_blah() {} // declare warning 17
        void blah_untypedSuffix() {} // declare warning 18       
    }
    
    static class Star {
        void starName() {} // declare warning 22
        void starPrefix_blah() {} // declare warning 23
        void blah_starSuffix() {} // declare warning  24      
    }
    static class Type{
        void typeName() {} // declare warning 27
        void typePrefix_blah() {} // declare warning 28
        void blah_typeSuffix() {} // declare warning 29      
    }
    
    static class TypePlus {    
        void typeplusName() {} // declare warning 33
        void typeplusPrefix_blah() {} // declare warning 34
        void blah_typeplusSuffix() {} // declare warning 35              
    }

    static class TypePlusSubtype extends TypePlus {
        void typeplusName() {} // declare warning 39
        void typeplusPrefix_blah() {} // declare warning 40
        void blah_typeplusSuffix() {} // declare warning  41              
    }
    
    // ------------------ tests
    declare @method: * untypedName() : @MtAn;
    declare @method: * untypedPrefix*() : @MtAn;
    declare @method: * *untypedSuffix() : @MtAn;

    declare @method: * *.starName() : @MtAn;
    declare @method: * *.starPrefix*() : @MtAn;
    declare @method: * *.*starSuffix() : @MtAn;
    
    declare @method: * Type.typeName() : @MtAn;
    declare @method: * Type.typePrefix*() : @MtAn;
    declare @method: * Type.*typeSuffix() : @MtAn;
    
    declare @method: * TypePlus+.typeplusName() : @MtAn;
    declare @method: * TypePlus+.typeplusPrefix*() : @MtAn;
    declare @method: * TypePlus+.*typeplusSuffix() : @MtAn;

    // ------------------ check using warnings, expected in .xml
    declare warning : execution(@MtAn * *()): "all";

}

