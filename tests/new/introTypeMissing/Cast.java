

import org.aspectj.testing.Tester; 

public class Cast {
    public static void main(String[] args) {
        Tester.expectEvent("cast");
        new TargetClass().introducedCast();
        Tester.checkAllEvents();
    }
}

aspect CastAspect {
    /** @testcase Introduced type unavailable to cast expressions in introduced methods */
    public void TargetClass.introducedCast() {
        boolean boolean_1 = getboolean();
        boolean boolean_2 = (boolean) getboolean();
        boolean boolean_3 = (boolean) this.getboolean();
        byte byte_1 = getbyte();
        byte byte_2 = (byte) getbyte();
        byte byte_3 = (byte) this.getbyte();
        char char_1 = getchar();
        char char_2 = (char) getchar();
        char char_3 = (char) this.getchar();
        short short_1 = getshort();
        short short_2 = (short) getshort();
        short short_3 = (short) this.getshort();
        int int_1 = getint();
        int int_2 = (int) getint();
        int int_3 = (int) this.getint();
        long long_1 = getlong();
        long long_2 = (long) getlong();
        long long_3 = (long) this.getlong();
        float float_1 = getfloat();
        float float_2 = (float) getfloat();
        float float_3 = (float) this.getfloat();
        double double_1 = getdouble();
        double double_2 = (double) getdouble();
        double double_3 = (double) this.getdouble();
        //X X_1 = getX();
        //X X_2 = (X) getX();
        //X X_3 = (X) this.getX();
        Util.signal("cast");
    }
}

