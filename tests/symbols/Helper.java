package symbols;

import org.aspectj.testing.Tester;

import org.aspectj.tools.ide.SymbolManager;
import org.aspectj.tools.ide.SourceLine;
import org.aspectj.tools.ide.Declaration;

import java.io.File;
import java.util.StringTokenizer;

public class Helper {

    public final SymbolManager sm = SymbolManager.getSymbolManager();

    public String file(String fn) {
        return new File(new File("symbols"),fn).getAbsolutePath();
    }

    public boolean allDecsFound = true;
    public Declaration getDecl(String fn, int ln) {
        Declaration dec = sm.getDeclarationAtLine(file(fn), ln);
        if (dec == null) {
            allDecsFound = false;
            Tester.checkFailed("Declaration at "+fn+":"+ln+" not found");
        }
        return dec;
    }

    public void checkPos(Declaration dec, int line1, int col1, int line2, int col2, String fname) {
        if (dec == null) return;
        Tester.checkEqual(dec.getBeginLine(),  line1, "wrond begin line of "+getName(dec));
        Tester.checkEqual(dec.getEndLine(),    line2, "wrond end line of "+getName(dec));
        Tester.checkEqual(dec.getBeginColumn(),col1,  "wrond begin column of "+getName(dec));
        Tester.checkEqual(dec.getEndColumn(),  col2,  "wrond end column of "+getName(dec));
        Tester.checkEqual(dec.getFilename(),   file(fname),  "wrond file name of "+getName(dec));
    }

    public void checkKind(Declaration dec, String kind) {
        if (dec == null) return;
        Tester.checkEqual(dec.getKind(),kind,"kinds differ");
    }

    public void checkSignature(Declaration dec, String sig, String fsig) {
        if (dec == null) return;
        String dsig = dec.getSignature();
        String dfsig = dec.getFullSignature();
        if (dsig != null) {
            dsig = dsig.trim();
            if (dsig.startsWith("/*") && dsig.endsWith("*/")) dsig = "";
        }
        if (dfsig != null) {
            dfsig = dfsig.trim();
            if (dfsig.startsWith("/*") && dfsig.endsWith("*/")) dfsig = "";
        }
        Tester.checkEqual(dsig,sig,"signatures of '"+getName(dec)+"' differ");
        Tester.checkEqual(dfsig,fsig,"full signatures of '"+getName(dec)+"' differ");
    }

    public void checkFormalComment(Declaration dec, String comment) {
        if (dec == null) return;
        String fc = dec.getFormalComment();
        if (fc != null) fc = fc.trim();
        //Tester.checkEqual(fc,comment,"formal comment differ");
    }

    public void checkPointsNothing(Declaration dec) {
        if (dec == null) return;
        Declaration[] points = dec.getPointsTo();
        if (points == null) {
            Tester.checkFailed(".getPointsTo() for Declaration of "+getName(dec)+" returns 'null'");
            return;
        }
        for (int i=0; i < points.length; i++) {
            Tester.checkFailed("unexpected that "+getName(dec)+" points to "+getName(points[i]));
        }
    }
    public void checkPointsTo(Declaration dec, Declaration[] expected) {
        if (dec == null) return;
        Declaration[] points = dec.getPointsTo();
        if (points == null) {
            Tester.checkFailed(".getPointsTo() for Declaration of "+getName(dec)+" returns 'null'");
            return;
        }
        int i, j;
        for (i=0; i < expected.length; i++) {
            if (expected[i] == null) {
                Tester.checkFailed("array element ["+i+"] of expected 'points to' declarations of "+getName(dec)+" contains 'null'");
                continue;
            }
            for (j=0; j < points.length; j++) {
                if (expected[i].equals(points[j])) break;
            }
            if (j >= points.length)
                Tester.checkFailed("expected that "+getName(dec)+" points to "+getName(expected[i]));
        }
        for (i=0; i < points.length; i++) {
            if (points[i] == null) {
                Tester.checkFailed(".getPointsTo() array element ["+i+"] of declaration "+getName(dec)+" contains 'null'");
                continue;
            }
            for (j=0; j < expected.length; j++) {
                if (points[i].equals(expected[j])) break;
            }
            if (j >= expected.length)
                Tester.checkFailed("unexpected that "+getName(dec)+" points to "+getName(points[i]));
        }
    }

    public void checkPointedToByNone(Declaration dec) {
        if (dec == null) return;
        Declaration[] pointed = dec.getPointedToBy();
        if (pointed == null) {
            Tester.checkFailed(".getPointedToBy() for Declaration of "+getName(dec)+" returns 'null'");
            return;
        }
        for (int i=0; i < pointed.length; i++) {
            Tester.checkFailed("unexpected that "+getName(dec)+" pointed by "+getName(pointed[i]));
        }
    }
    public void checkPointedToBy(Declaration dec, Declaration[] expected) {
        if (dec == null) return;
        Declaration[] pointed = dec.getPointedToBy();
        if (pointed == null) {
            Tester.checkFailed(".getPointedToBy() for Declaration of "+getName(dec)+" returns 'null'");
            return;
        }
        int i, j;
        for (i=0; i < expected.length; i++) {
            if (expected[i] == null) {
                Tester.checkFailed("array element ["+i+"] of expected 'pointed to by' declarations of "+getName(dec)+" contains 'null'");
                continue;
            }
            for (j=0; j < pointed.length; j++) {
                if (pointed[j].equals(expected[i])) break;
            }
            if (j >= pointed.length)
                Tester.checkFailed("expected that "+getName(dec)+" pointed to by "+getName(expected[i]));
        }
        for (i=0; i < pointed.length; i++) {
            if (pointed[i] == null) {
                Tester.checkFailed(".getPointedToBy() array element ["+i+"] of declaration "+getName(dec)+" contains 'null'");
                continue;
            }
            for (j=0; j < expected.length; j++) {
                if (pointed[i].equals(expected[j])) break;
            }
            if (j >= expected.length)
                Tester.checkFailed("unexpected that "+getName(dec)+" pointed to by "+getName(pointed[i]));
        }
    }

    public void checkModifiers(Declaration dec, String expected_modifiers) {
        if (dec == null) return;
        StringTokenizer st = new StringTokenizer(expected_modifiers);
        String[] expected = new String[st.countTokens()];
        for(int i=0; i < expected.length; i++) expected[i] = st.nextToken();
        st = new StringTokenizer(dec.getModifiers());
        String[] modifiers = new String[st.countTokens()];
        for(int i=0; i < modifiers.length; i++) modifiers[i] = st.nextToken();

        int i, j;
        for (i=0; i < expected.length; i++) {
            for (j=0; j < modifiers.length; j++) {
                if (modifiers[j].equals(expected[i])) break;
            }
            if (j >= modifiers.length)
                Tester.checkFailed("expected that "+getName(dec)+" has modifier "+expected[i]);
        }
        for (i=0; i < modifiers.length; i++) {
            for (j=0; j < expected.length; j++) {
                if (modifiers[i].equals(expected[j])) break;
            }
            if (j >= expected.length)
                Tester.checkFailed("unexpected that "+getName(dec)+" has modifier "+modifiers[i]);
        }
    }

    public void checkAllDecsOf(Declaration dec, Declaration[] decs) {
        Declaration[] sdecs = dec.getDeclarations();
        if (sdecs == null) {
            Tester.checkFailed("unexpected that 'getDeclarations' for "+getName(dec)+" returned 'null'");
            return;
        }
        int i, j;
        for (i=0; i < decs.length; i++) {
            if (decs[i] == null) continue;
            for (j=0; j < sdecs.length; j++) {
                if (decs[j].equals(sdecs[i])) break;
            }
            if (j >= sdecs.length)
                Tester.checkFailed("expected that "+getName(dec)+" contains "+getName(decs[i]));
        }
        for (i=0; i < sdecs.length; i++) {
            if (sdecs[i] == null) continue;
            for (j=0; j < decs.length; j++) {
                if (sdecs[i].equals(decs[j])) break;
            }
            if (j >= decs.length)
                Tester.checkFailed("unexpected that "+getName(dec)+" contains "+getName(sdecs[i]));
        }
    }

    private static String getName(Declaration dec) {
        if (dec == null) return "<null>";
        String name = dec.getSignature();
        if (name != null && name.length() > 0) return name + "(" + getFilePos(dec) + ")";
        name = dec.getKind() + " at " + getFilePos(dec);
        return name;
    }
    private static String getFilePos(Declaration dec) {
        String longFileName = dec.getFilename();
        if (longFileName == null) return "?.java:" + dec.getBeginLine();
        int pos = longFileName.lastIndexOf('/');
        if (pos < 0) pos = longFileName.lastIndexOf('\\');
        if (pos < 0) return longFileName + ":" + dec.getBeginLine();
        return longFileName.substring(pos+1) + ":" + dec.getBeginLine();
    }
}

