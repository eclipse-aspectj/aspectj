


// NOTE: the presence of this Aspect, together with ServiceCall.aj
// seems to trigger the infinite loop on the second or third re-build.
//
// Sometimes it triggered even if the @NeedsXYZ isn't used at all

public aspect FactoryMarker
{
    
    public interface BootSpringKontext { };
    
    declare parents : @NeedsXYZ * implements BootSpringKontext;
    
    public Object[] BootSpringKontext.loadXYZ() {
        return new Object[] {"load it"};
    }
    
    
    declare @method : Object[] loadXYZ*() : @Factory;

}
