

import org.aspectj.testing.Tester; 
import org.aspectj.testing.Tester;

public class CastInFieldInit {
    public static void main(String[] args) {
        Tester.expectEvent("castfield");
        TargetClass me = new TargetClass();
        Tester.check(me.result, "me.result");
        Tester.checkAllEvents();
    }
}

interface Valid  { public boolean valid();}

aspect CastInFieldInitAspect {
    /** @testcase Introduced type unavailable to cast expressions in introduced field initializers */ 
    public boolean TargetClass.result =
        new Valid() {
                public boolean valid() {
                    boolean boolean_1 = getboolean();
                    boolean boolean_2 = (boolean) getboolean();
                    //boolean boolean_3 = (boolean) TargetClass.this.getboolean();
                    byte byte_1 = getbyte();
                    byte byte_2 = (byte) getbyte();
                    //byte byte_3 = (byte) TargetClass.this.getbyte();
                    char char_1 = getchar();
                    char char_2 = (char) getchar();
                    //char char_3 = (char) TargetClass.this.getchar();
                    short short_1 = getshort();
                    short short_2 = (short) getshort();
                    //short short_3 = (short) TargetClass.this.getshort();
                    int int_1 = getint();
                    int int_2 = (int) getint();
                    //int int_3 = (int) TargetClass.this.getint();
                    long long_1 = getlong();
                    long long_2 = (long) getlong();
                    //long long_3 = (long) TargetClass.this.getlong();
                    float float_1 = getfloat();
                    float float_2 = (float) getfloat();
                    //float float_3 = (float) TargetClass.this.getfloat();
                    double double_1 = getdouble();
                    double double_2 = (double) getdouble();
                    //double double_3 = (double) TargetClass.this.getdouble();
                    //X X_1 = getX();
                    //X X_2 = (X) getX();
                    //X X_3 = (X) this.getX();
                    Util.signal("castfield");
                    return (boolean_1 && boolean_2 );
                }
            }.valid();
}

