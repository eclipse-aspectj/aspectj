
package pack;

import util.Util;

public class PublicTarget {
    public    static int publicStaticInt = 1;
    protected static int protectedStaticInt = 1;
              static int defaultStaticInt = 1;
    private   static int privateStaticInt = 1;
    public           int publicInt = 1;
    protected        int protectedInt = 1;
                     int defaultInt = 1;
    private          int privateInt = 1;
    public           void publicMethod()    { Util.signal(Util.pubPublic); }
    protected        void protectedMethod() { Util.signal(Util.pubProtected); }
                     void defaultMethod()   { Util.signal(Util.pubDefault); }
    private          void privateMethod()   { Util.signal(Util.pubPrivate); }
    
    public static void readPublicTarget() {
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

    public class PublicInner {
        public    static final int publicStaticPublicInnerInt = 1;
        protected static final int protectedStaticPublicInnerInt = 1;
        /* def */ static final int defaultStaticPublicInnerInt = 1;
        private   static final int privateStaticPublicInnerInt = 1;
        public                 int publicPublicInnerInt = 1;
        protected              int protectedPublicInnerInt = 1;
        /* default */          int defaultPublicInnerInt = 1;
        private                int privatePublicInnerInt = 1;
        public           void publicPublicInnerMethod()    { Util.signal(Util.pubInnerPublic); }
        protected        void protectedPublicInnerMethod() { Util.signal(Util.pubInnerProtected); }
        /* default */    void defaultPublicInnerMethod()   { Util.signal(Util.pubInnerDefault); }
        private          void privatePublicInnerMethod()   { Util.signal(Util.pubInnerPrivate); }
    
        public void readPublicInnerTarget() {
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
    }
}
