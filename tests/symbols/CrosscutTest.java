import symbols.Helper;
import org.aspectj.testing.Tester;

import org.aspectj.tools.ide.SymbolManager;
import org.aspectj.tools.ide.SourceLine;
import org.aspectj.tools.ide.Declaration;

import java.io.File;

public class CrosscutTest {
    private static Helper h = new Helper();

    public static void main(String[] args) {
        Declaration classDec      = h.getDecl("C.java", 3);
        Declaration aspectDec     = h.getDecl("A.java", 3);

        Declaration methodDecV    = h.getDecl("C.java", 8);
        Declaration methodDecVI   = h.getDecl("C.java", 9);
        Declaration methodDecVLF  = h.getDecl("C.java",10);
        Declaration methodDecISO  = h.getDecl("C.java",11);

        Declaration fieldDecI     = h.getDecl("C.java",13);
        Declaration fieldDecF     = h.getDecl("C.java",14);

        Declaration pointcutDecInstOf     = h.getDecl("A.java", 6);
        Declaration pointcutDecHasAsp     = h.getDecl("A.java", 7);
        Declaration pointcutDecWithin     = h.getDecl("A.java", 10);
        Declaration pointcutDecWithinAll  = h.getDecl("A.java", 11);
        Declaration pointcutDecWithinCode = h.getDecl("A.java", 12);
        Declaration pointcutDecCFlow      = h.getDecl("A.java", 15);
        Declaration pointcutDecCFlowTop   = h.getDecl("A.java", 16);
        Declaration pointcutDecCalls      = h.getDecl("A.java", 19);
        Declaration pointcutDecRec        = h.getDecl("A.java", 20);
        Declaration pointcutDecExec       = h.getDecl("A.java", 21);
        Declaration pointcutDecCallsTo    = h.getDecl("A.java", 22);
        Declaration pointcutDecHandThr    = h.getDecl("A.java", 25);
        Declaration pointcutDecHandErr    = h.getDecl("A.java", 26);
        Declaration pointcutDecHandExc    = h.getDecl("A.java", 27);
        Declaration pointcutDecHandRt     = h.getDecl("A.java", 28);
        Declaration pointcutDecGets       = h.getDecl("A.java", 31);
        Declaration pointcutDecSets       = h.getDecl("A.java", 32);

        Declaration adviceDecBefore     = h.getDecl("A.java", 35);
        Declaration adviceDecAfter      = h.getDecl("A.java", 36);
        Declaration adviceDecAfterRet   = h.getDecl("A.java", 37);
        Declaration adviceDecAfterThr   = h.getDecl("A.java", 38);
        Declaration adviceDecAround     = h.getDecl("A.java", 41);

        Declaration fieldDecIntrD     = h.getDecl("A.java", 44);
        Declaration methodDecIntrV    = h.getDecl("A.java", 45);

        Declaration adviceDecBeforeToString    = h.getDecl("A.java", 47);
        Declaration adviceDecBeforeNew         = h.getDecl("A.java", 48);

        if (!h.allDecsFound) return;

        // Check "points to"
        h.checkPointsNothing(methodDecV);
        h.checkPointsNothing(methodDecVI);
        h.checkPointsNothing(methodDecVLF);
        h.checkPointsNothing(methodDecISO);

        h.checkPointsNothing(pointcutDecInstOf);
        h.checkPointsNothing(pointcutDecHasAsp);
        h.checkPointsNothing(pointcutDecWithin);
        h.checkPointsNothing(pointcutDecWithinAll);
        h.checkPointsNothing(pointcutDecWithinCode);
        h.checkPointsNothing(pointcutDecCFlow);
        h.checkPointsNothing(pointcutDecCFlowTop);
        h.checkPointsNothing(pointcutDecCalls);
        h.checkPointsNothing(pointcutDecRec);
        h.checkPointsNothing(pointcutDecExec);
        h.checkPointsNothing(pointcutDecCallsTo);
        h.checkPointsNothing(pointcutDecHandThr);
        h.checkPointsNothing(pointcutDecHandErr);
        h.checkPointsNothing(pointcutDecHandExc);
        h.checkPointsNothing(pointcutDecHandRt);
        h.checkPointsNothing(pointcutDecGets);
        h.checkPointsNothing(pointcutDecSets);

        //XXX Need to decide if advices on introductions have to point to
        // the introduction declaration, or to the member that was introduced
        h.checkPointsTo(adviceDecBefore,   new Declaration[]{methodDecV,methodDecIntrV});
        h.checkPointsTo(adviceDecAfter,    new Declaration[]{methodDecV,methodDecIntrV});
        h.checkPointsTo(adviceDecAfterRet, new Declaration[]{methodDecISO});
        h.checkPointsTo(adviceDecAfterThr, new Declaration[]{methodDecV});
        h.checkPointsTo(adviceDecAround,   new Declaration[]{methodDecV});

        h.checkPointsNothing(adviceDecBeforeToString);
        h.checkPointsNothing(adviceDecBeforeNew);

        // Check "pointed by"
        h.checkPointedToBy(methodDecV, new Declaration[]{adviceDecBefore,adviceDecAfter,adviceDecAfterThr,adviceDecAround});
        h.checkPointedToByNone(methodDecVI);
        h.checkPointedToByNone(methodDecVLF);
        h.checkPointedToBy(methodDecISO, new Declaration[]{adviceDecAfterRet});

        h.checkPointedToByNone(pointcutDecInstOf);
        h.checkPointedToByNone(pointcutDecHasAsp);
        h.checkPointedToByNone(pointcutDecWithin);
        h.checkPointedToByNone(pointcutDecWithinAll);
        h.checkPointedToByNone(pointcutDecWithinCode);
        h.checkPointedToByNone(pointcutDecCFlow);
        h.checkPointedToByNone(pointcutDecCFlowTop);
        h.checkPointedToByNone(pointcutDecCalls);
        h.checkPointedToByNone(pointcutDecRec);
        h.checkPointedToByNone(pointcutDecExec);
        h.checkPointedToByNone(pointcutDecCallsTo);
        h.checkPointedToByNone(pointcutDecHandThr);
        h.checkPointedToByNone(pointcutDecHandErr);
        h.checkPointedToByNone(pointcutDecHandExc);
        h.checkPointedToByNone(pointcutDecHandRt);
        h.checkPointedToByNone(pointcutDecGets);
        h.checkPointedToByNone(pointcutDecSets);

        h.checkPointedToByNone(adviceDecBefore);
        h.checkPointedToByNone(adviceDecBefore);
        h.checkPointedToByNone(adviceDecAfter);
        h.checkPointedToByNone(adviceDecAfterRet);
        h.checkPointedToByNone(adviceDecAfterThr);
        h.checkPointedToByNone(adviceDecAround);

        h.checkPointedToByNone(adviceDecBeforeToString);
        h.checkPointedToByNone(adviceDecBeforeNew);
    }

}

