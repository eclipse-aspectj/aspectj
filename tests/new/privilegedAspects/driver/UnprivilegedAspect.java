
package driver;

// import pack.DefaultTarget; // does not work - ok 
import pack.PublicTarget; 

/**
 * This is a copy of PrivilegedAspect, but without the privilege.
 * It is an error test for unprivileged access to target classes
 * esp. when compiled at the same time as PrivilegedAspect.
 * todo: remove this case or render manageable.
 */
// Keep in sync with PrivilegedAspect
public aspect UnprivilegedAspect {
    /** run all other static methods 
     * @param args ignored */
    public static void main(String[] args) {
        readPublicTarget();
        readPublicInnerTarget();
        readDefaultTarget();
        readDefaultInnerTarget();
    }

    /** in public class 
     * read static and instance fields and 
     * invoke methods, both of all access types 
     */
    public static void readPublicTarget() {
        Class c = PublicTarget.class;
        int i = 0;
        i += PublicTarget.publicStaticInt;
        i += PublicTarget.protectedStaticInt;
        i += PublicTarget.defaultStaticInt;
        i += PublicTarget.privateStaticInt;
        PublicTarget publicTarget = new PublicTarget();
        i += publicTarget.publicInt;
        i += publicTarget.protectedInt;
        i += publicTarget.defaultInt;
        i += publicTarget. privateInt;
        publicTarget.publicMethod(); 
        publicTarget.protectedMethod();
        publicTarget.defaultMethod();
        publicTarget.privateMethod();
    }

    /** in public inner class 
     * read static and instance fields and 
     * invoke methods, both of all access types 
     */
    public static void readPublicInnerTarget() {
        Class c = PublicTarget.PublicInner.class;
        int i = 0;
        i += PublicTarget.PublicInner.publicStaticPublicInnerInt;
        i += PublicTarget.PublicInner.protectedStaticPublicInnerInt;
        i += PublicTarget.PublicInner.defaultStaticPublicInnerInt;
        i += PublicTarget.PublicInner.privateStaticPublicInnerInt;
        PublicTarget.PublicInner publicInnerTarget 
            = new PublicTarget().new PublicInner();
        i += publicInnerTarget.publicPublicInnerInt;
        i += publicInnerTarget.protectedPublicInnerInt;
        i += publicInnerTarget.defaultPublicInnerInt;
        i += publicInnerTarget. privatePublicInnerInt;
        publicInnerTarget.publicPublicInnerMethod(); 
        publicInnerTarget.protectedPublicInnerMethod();
        publicInnerTarget.defaultPublicInnerMethod();
        publicInnerTarget.privatePublicInnerMethod();
    }

    /** in class with default access 
     * read static and instance fields and 
     * invoke methods, both of all access types 
     */
    public static void readDefaultTarget() {
        Class c = pack.DefaultTarget.class;
        int i = 0;
        i += pack.DefaultTarget.publicStaticInt;
        i += pack.DefaultTarget.protectedStaticInt;
        i += pack.DefaultTarget.defaultStaticInt;
        i += pack.DefaultTarget.privateStaticInt;
        pack.DefaultTarget defaultTarget = new pack.DefaultTarget();
        i += defaultTarget.publicInt;
        i += defaultTarget.protectedInt;
        i += defaultTarget.defaultInt;
        i += defaultTarget. privateInt;
        defaultTarget.publicMethod(); 
        defaultTarget.protectedMethod();
        defaultTarget.defaultMethod();
        defaultTarget.privateMethod();
    }

    /** in inner class with default access 
     * read static and instance fields and 
     * invoke methods, both of all access types 
     */
    public static void readDefaultInnerTarget() {
        Class c = pack.DefaultTarget.DefaultInner.class;
        int i = 0;
        i += pack.DefaultTarget.DefaultInner.publicStaticDefaultInnerInt;
        i += pack.DefaultTarget.DefaultInner.protectedStaticDefaultInnerInt;
        i += pack.DefaultTarget.DefaultInner.defaultStaticDefaultInnerInt;
        i += pack.DefaultTarget.DefaultInner.privateStaticDefaultInnerInt;
        pack.DefaultTarget.DefaultInner defaultInnerTarget 
            = new pack.DefaultTarget().new DefaultInner();
        i += defaultInnerTarget.publicDefaultInnerInt;
        i += defaultInnerTarget.protectedDefaultInnerInt;
        i += defaultInnerTarget.defaultDefaultInnerInt;
        i += defaultInnerTarget.privateDefaultInnerInt;
        defaultInnerTarget.publicDefaultInnerMethod(); 
        defaultInnerTarget.protectedDefaultInnerMethod();
        defaultInnerTarget.defaultDefaultInnerMethod();
        defaultInnerTarget.privateDefaultInnerMethod();
    }
}
