import java.awt.Color;

public class LocalInitializerVariableNotBlank {

    static Color[] greys;

    static {
        greys=new Color[16];
        int gr;
        for(int i=0;i<greys.length;i++) {
            gr=(int)(170.0*(1.0-Math.pow(i/15.0,2.3)));
            greys[i]=new Color(gr,gr,gr);
        }
    }

    public static void main(String[] args) {
    }
}
