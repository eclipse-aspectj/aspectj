
package util;

import org.aspectj.testing.Tester;
import org.aspectj.testing.Tester; 

public class Util {
    public static       boolean PRINT = false;
    public static final String pubPublic         = "PublicTarget.publicPublicMethod";
    public static final String pubProtected      = "PublicTarget.protectedPublicMethod";
    public static final String pubDefault        = "PublicTarget.defaultPublicMethod";
    public static final String pubPrivate        = "PublicTarget.privatePublicMethod";
    public static final String pubInnerPublic    = "PublicTarget.publicPublicInnerMethod";
    public static final String pubInnerProtected = "PublicTarget.protectedPublicInnerMethod";
    public static final String pubInnerDefault   = "PublicTarget.defaultPublicInnerMethod";
    public static final String pubInnerPrivate   = "PublicTarget.privatePublicInnerMethod";
    public static final String defPublic         = "DefaultTarget.publicDefaultMethod";
    public static final String defProtected      = "DefaultTarget.protectedDefaultMethod";
    public static final String defDefault        = "DefaultTarget.defaultDefaultMethod";
    public static final String defPrivate        = "DefaultTarget.privateDefaultMethod";
    public static final String defInnerPublic    = "DefaultTarget.publicDefaultInnerMethod";
    public static final String defInnerProtected = "DefaultTarget.protectedDefaultInnerMethod";
    public static final String defInnerDefault   = "DefaultTarget.defaultDefaultInnerMethod";
    public static final String defInnerPrivate   = "DefaultTarget.privateDefaultInnerMethod";

    /** signal some test event for later validation 
     * if PRINT, also outputs to System.err
     */
    public static void signal(String s) {
        if (PRINT) {
            System.err.println(" Util.signal: " + s);
        }
        Tester.event(s);
    }
}
