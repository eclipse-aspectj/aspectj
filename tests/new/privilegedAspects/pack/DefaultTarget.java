
package pack;

import util.Util;

/* copy/paste of PublicTarget with mods to default */
class DefaultTarget {
    public    static int publicStaticInt = 1;
    protected static int protectedStaticInt = 1;
              static int defaultStaticInt = 1;
    private   static int privateStaticInt = 1;
    public           int publicInt = 1;
    protected        int protectedInt = 1;
                     int defaultInt = 1;
    private          int privateInt = 1;
    public           void publicMethod()    { Util.signal(Util.defPublic); }
    protected        void protectedMethod() { Util.signal(Util.defProtected); }
                     void defaultMethod()   { Util.signal(Util.defDefault); }
    private          void privateMethod()   { Util.signal(Util.defPrivate); }
    
    public static void readDefaultTarget() {
        int i = 0;
        i += DefaultTarget.publicStaticInt;
        i += DefaultTarget.protectedStaticInt;
        i += DefaultTarget.defaultStaticInt;
        i += DefaultTarget.privateStaticInt;
        DefaultTarget defaultTarget = new DefaultTarget();
        i += defaultTarget.publicInt;
        i += defaultTarget.protectedInt;
        i += defaultTarget.defaultInt;
        i += defaultTarget. privateInt;
        defaultTarget.publicMethod(); 
        defaultTarget.protectedMethod();
        defaultTarget.defaultMethod();
        defaultTarget.privateMethod();
    }

    class DefaultInner {
        public    static final int publicStaticDefaultInnerInt = 1;
        protected static final int protectedStaticDefaultInnerInt = 1;
        /* def */ static final int defaultStaticDefaultInnerInt = 1;
        private   static final int privateStaticDefaultInnerInt = 1;
        public                 int publicDefaultInnerInt = 1;
        protected              int protectedDefaultInnerInt = 1;
        /* default */          int defaultDefaultInnerInt = 1;
        private                int privateDefaultInnerInt = 1;
        public           void publicDefaultInnerMethod()    { Util.signal(Util.defInnerPublic); }
        protected        void protectedDefaultInnerMethod() { Util.signal(Util.defInnerProtected); }
        /* default */    void defaultDefaultInnerMethod()   { Util.signal(Util.defInnerDefault); }
        private          void privateDefaultInnerMethod()   { Util.signal(Util.defInnerPrivate); }
    
        public void readDefaultInnerTarget() {
            int i = 0;
            i += DefaultTarget.DefaultInner.publicStaticDefaultInnerInt;
            i += DefaultTarget.DefaultInner.protectedStaticDefaultInnerInt;
            i += DefaultTarget.DefaultInner.defaultStaticDefaultInnerInt;
            i += DefaultTarget.DefaultInner.privateStaticDefaultInnerInt;
            DefaultTarget.DefaultInner defaultInnerTarget 
                = new DefaultTarget().new DefaultInner();
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
}
