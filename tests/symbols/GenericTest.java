import symbols.Helper;
import org.aspectj.testing.Tester;

import org.aspectj.tools.ide.SymbolManager;
import org.aspectj.tools.ide.SourceLine;
import org.aspectj.tools.ide.Declaration;

import java.io.File;

public class GenericTest {
    private static Helper h = new Helper();

    public static void main(String[] args) {
        Declaration classDec      = h.getDecl("C.java", 3);
        Declaration aspectDec     = h.getDecl("A.java", 3);

        Declaration fieldDecI     = h.getDecl("C.java", 13);
        Declaration fieldDecF     = h.getDecl("C.java", 14);

        //Declaration initDecS      = h.getDecl("C.java", 22);
        //Declaration initDec       = h.getDecl("C.java", 26);

        Declaration methodDecV    = h.getDecl("C.java", 8);
        Declaration methodDecVI   = h.getDecl("C.java", 9);
        Declaration methodDecVLF  = h.getDecl("C.java",10);
        Declaration methodDecISO  = h.getDecl("C.java",11);

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
        Declaration adviceDecBeforeToString    = h.getDecl("A.java", 47);
        Declaration adviceDecBeforeNew         = h.getDecl("A.java", 48);


        Declaration fieldDecIntrD     = h.getDecl("A.java", 44);
        Declaration methodDecIntrV    = h.getDecl("A.java", 45);

        // check we've checking all decs in aspect A and class C
        h.checkAllDecsOf(classDec,new Declaration[]{
            fieldDecI,
            fieldDecF,
            //initDecS,
            //initDec,
            methodDecV,
            methodDecVI,
            methodDecVLF,
            methodDecISO
        });
        h.checkAllDecsOf(aspectDec,new Declaration[]{
            pointcutDecInstOf,
            pointcutDecHasAsp,
            pointcutDecWithin,
            pointcutDecWithinAll,
            pointcutDecWithinCode,
            pointcutDecCFlow,
            pointcutDecCFlowTop,
            pointcutDecCalls,
            pointcutDecRec,
            pointcutDecExec,
            pointcutDecCallsTo,
            pointcutDecHandThr,
            pointcutDecHandErr,
            pointcutDecHandExc,
            pointcutDecHandRt,
            pointcutDecGets,
            pointcutDecSets,
            adviceDecBefore,
            adviceDecAfter,
            adviceDecAfterRet,
            adviceDecAfterThr,
            adviceDecAround,
            adviceDecBeforeToString,
            adviceDecBeforeNew,
            fieldDecIntrD,
            methodDecIntrV
        });

        if (!h.allDecsFound) return;

        h.checkPos(classDec,     3, 1,24, 2,  "C.java");
        h.checkPos(aspectDec,    3, 1,49, 2,  "A.java");
        h.checkPos(fieldDecI,   13, 5,13,34,  "C.java");
        h.checkPos(fieldDecF,   14, 5,14,20,  "C.java");
        h.checkPos(methodDecV,   8, 5, 8, 21,  "C.java");
        h.checkPos(methodDecVI,  9, 5, 9, 34,  "C.java");
        h.checkPos(methodDecVLF,10, 5,10, 65,  "C.java");
        h.checkPos(methodDecISO,11, 5,11, 74,  "C.java");
        h.checkPos(pointcutDecInstOf,     6,  5,  6, 44, "A.java");
        h.checkPos(pointcutDecHasAsp,     7,  5,  7, 42, "A.java");
        h.checkPos(pointcutDecWithin,    10,  5, 10, 36, "A.java");
        h.checkPos(pointcutDecWithinAll, 11,  5, 11, 42, "A.java");
        h.checkPos(pointcutDecWithinCode,12,  5, 12, 55, "A.java");
        h.checkPos(pointcutDecCFlow,     15,  5, 15, 47, "A.java");
        h.checkPos(pointcutDecCFlowTop,  16,  5, 16, 53, "A.java");
        h.checkPos(pointcutDecCalls,     19,  5, 19, 44, "A.java");
        h.checkPos(pointcutDecRec,       20,  5, 20, 54, "A.java");
        h.checkPos(pointcutDecExec,      21,  5, 21, 61, "A.java");
        h.checkPos(pointcutDecCallsTo,   22,  5, 22, 59, "A.java");
        h.checkPos(pointcutDecHandThr,   25,  5, 25, 60, "A.java");
        h.checkPos(pointcutDecHandErr,   26,  5, 26, 56, "A.java");
        h.checkPos(pointcutDecHandExc,   27,  5, 27, 60, "A.java");
        h.checkPos(pointcutDecHandRt,    28,  5, 28, 66, "A.java");
        h.checkPos(pointcutDecGets,      31,  5, 31, 40, "A.java");
        h.checkPos(pointcutDecSets,      32,  5, 32, 40, "A.java");
        h.checkPos(adviceDecBefore,     35,  5, 35, 41, "A.java");
        h.checkPos(adviceDecAfter,      36,  5, 36, 40, "A.java");
        h.checkPos(adviceDecAfterRet,   37,  5, 37, 59, "A.java");
        h.checkPos(adviceDecAfterThr,   38,  5, 40,  6, "A.java");
        h.checkPos(adviceDecAround,     41,  5, 41, 69, "A.java");
        h.checkPos(fieldDecIntrD,       44,  5, 44, 35, "A.java");
        h.checkPos(methodDecIntrV,      45,  5, 45, 47, "A.java");

        h.checkKind(classDec,     "class");
        h.checkKind(aspectDec,    "class");
        h.checkKind(fieldDecI,    "field");
        h.checkKind(fieldDecF,    "field");
        h.checkKind(methodDecV,   "method");
        h.checkKind(methodDecVI,  "method");
        h.checkKind(methodDecVLF, "method");
        h.checkKind(methodDecISO, "method");
        h.checkKind(pointcutDecInstOf,    "pointcut");
        h.checkKind(pointcutDecHasAsp,    "pointcut");
        h.checkKind(pointcutDecWithin,    "pointcut");
        h.checkKind(pointcutDecWithinAll, "pointcut");
        h.checkKind(pointcutDecWithinCode,"pointcut");
        h.checkKind(pointcutDecCFlow,     "pointcut");
        h.checkKind(pointcutDecCFlowTop,  "pointcut");
        h.checkKind(pointcutDecCalls,     "pointcut");
        h.checkKind(pointcutDecRec,       "pointcut");
        h.checkKind(pointcutDecExec,      "pointcut");
        h.checkKind(pointcutDecCallsTo,   "pointcut");
        h.checkKind(pointcutDecHandThr,   "pointcut");
        h.checkKind(pointcutDecHandErr,   "pointcut");
        h.checkKind(pointcutDecHandExc,   "pointcut");
        h.checkKind(pointcutDecHandRt,    "pointcut");
        h.checkKind(pointcutDecGets,      "pointcut");
        h.checkKind(pointcutDecSets,      "pointcut");
        h.checkKind(adviceDecBefore,     "advice");
        h.checkKind(adviceDecAfter,      "advice");
        h.checkKind(adviceDecAfterRet,   "advice");
        h.checkKind(adviceDecAfterThr,   "advice");
        h.checkKind(adviceDecAround,     "advice");
        h.checkKind(fieldDecIntrD,  "introduction");
        h.checkKind(methodDecIntrV, "introduction");

        h.checkFormalComment(classDec,     "");
        h.checkFormalComment(aspectDec,    "");
        h.checkFormalComment(fieldDecI,    "");
        h.checkFormalComment(fieldDecF,    "");
        h.checkFormalComment(methodDecV,   "/**\n     * multiline\n     * comment\n    */");
        h.checkFormalComment(methodDecVI,  "");
        h.checkFormalComment(methodDecVLF, "");
        h.checkFormalComment(methodDecISO, "");
        h.checkFormalComment(pointcutDecInstOf,    "/** objects */");
        h.checkFormalComment(pointcutDecHasAsp,    "");
        h.checkFormalComment(pointcutDecWithin,    "/** lexical extents */");
        h.checkFormalComment(pointcutDecWithinAll, "");
        h.checkFormalComment(pointcutDecWithinCode,"");
        h.checkFormalComment(pointcutDecCFlow,     "/** control flow */");
        h.checkFormalComment(pointcutDecCFlowTop,  "");
        h.checkFormalComment(pointcutDecCalls,     "/** methods and constructors */");
        h.checkFormalComment(pointcutDecRec,       "");
        h.checkFormalComment(pointcutDecExec,      "");
        h.checkFormalComment(pointcutDecCallsTo,   "");
        h.checkFormalComment(pointcutDecHandThr,   "/** exception handlers */");
        h.checkFormalComment(pointcutDecHandErr,   "");
        h.checkFormalComment(pointcutDecHandExc,   "");
        h.checkFormalComment(pointcutDecHandRt,    "");
        h.checkFormalComment(pointcutDecGets,      "/** fields */");
        h.checkFormalComment(pointcutDecSets,      "");
        h.checkFormalComment(adviceDecBefore,     "/** Advices */");
        h.checkFormalComment(adviceDecAfter,      "");
        h.checkFormalComment(adviceDecAfterRet,   "");
        h.checkFormalComment(adviceDecAfterThr,   "");
        h.checkFormalComment(adviceDecAround,     "");
        h.checkFormalComment(fieldDecIntrD,       "/** Introductions */");
        h.checkFormalComment(methodDecIntrV,      "");

        h.checkModifiers(classDec,     "public");
        h.checkModifiers(aspectDec,    "public strictfp");
        h.checkModifiers(fieldDecI,    "public static volatile");
        h.checkModifiers(fieldDecF,    "public");
        h.checkModifiers(methodDecV,   "");
        h.checkModifiers(methodDecVI,  "public");
        h.checkModifiers(methodDecVLF, "synchronized public static");
        h.checkModifiers(methodDecISO, "");
        h.checkModifiers(pointcutDecInstOf,    "");
        h.checkModifiers(pointcutDecHasAsp,    "");
        h.checkModifiers(pointcutDecWithin,    "");
        h.checkModifiers(pointcutDecWithinAll, "");
        h.checkModifiers(pointcutDecWithinCode,"");
        h.checkModifiers(pointcutDecCFlow,     "");
        h.checkModifiers(pointcutDecCFlowTop,  "");
        h.checkModifiers(pointcutDecCalls,     "");
        h.checkModifiers(pointcutDecRec,       "");
        h.checkModifiers(pointcutDecExec,      "");
        h.checkModifiers(pointcutDecCallsTo,   "");
        h.checkModifiers(pointcutDecHandThr,   "");
        h.checkModifiers(pointcutDecHandErr,   "");
        h.checkModifiers(pointcutDecHandExc,   "");
        h.checkModifiers(pointcutDecHandRt,    "");
        h.checkModifiers(pointcutDecGets,      "");
        h.checkModifiers(pointcutDecSets,      "");
        h.checkModifiers(adviceDecBefore,   "");
        h.checkModifiers(adviceDecAfter,    "");
        h.checkModifiers(adviceDecAfterRet, "");
        h.checkModifiers(adviceDecAfterThr, "");
        h.checkModifiers(adviceDecAround,   "");
        h.checkModifiers(fieldDecIntrD,     "public");
        h.checkModifiers(methodDecIntrV,    "private");

        h.checkSignature(classDec,     "C", "public class C");
        h.checkSignature(aspectDec,    "A", "public strictfp class A");
        h.checkSignature(fieldDecI,    "i", "public static volatile int i");
        h.checkSignature(fieldDecF,    "f", "public float f");
        h.checkSignature(methodDecV,   "MethV()", "void MethV()");
        h.checkSignature(methodDecVI,  "MethVI(int)", "public void MethVI(int i)");
        h.checkSignature(methodDecVLF, "MethVLF(long, float)", "public static synchronized void MethVLF(long l, float f)");
        h.checkSignature(methodDecISO, "MethISO(String, Object)", "int MethISO(java.lang.String s, java.lang.Object o)");
        h.checkSignature(pointcutDecInstOf,    "instanceof_C()", "");
        h.checkSignature(pointcutDecHasAsp,    "hasaspect_A()", "");
        h.checkSignature(pointcutDecWithin,    "within_C()", "");
        h.checkSignature(pointcutDecWithinAll, "withinall_C()", "");
        h.checkSignature(pointcutDecWithinCode,"withincode_C()", "");
        h.checkSignature(pointcutDecCFlow,     "cflow_C()", "");
        h.checkSignature(pointcutDecCFlowTop,  "cflowtop_C()", "");
        h.checkSignature(pointcutDecCalls,     "calls_C()", "");
        h.checkSignature(pointcutDecRec,       "receptions_C()", "");
        h.checkSignature(pointcutDecExec,      "executions_C()", "");
        h.checkSignature(pointcutDecCallsTo,   "callsto_C()", "");
        h.checkSignature(pointcutDecHandThr,   "handlers_Thr()", "");
        h.checkSignature(pointcutDecHandErr,   "handlers_Err()", "");
        h.checkSignature(pointcutDecHandExc,   "handlers_Exc()", "");
        h.checkSignature(pointcutDecHandRt,    "handlers_Rt()", "");
        h.checkSignature(pointcutDecGets,      "gets_f()", "");
        h.checkSignature(pointcutDecSets,      "sets_f()", "");
        h.checkSignature(adviceDecBefore,   "before()", "");
        h.checkSignature(adviceDecAfter,    "after()", "");
        h.checkSignature(adviceDecAfterRet, "afterReturning()", "");
        h.checkSignature(adviceDecAfterThr, "afterThrowing()", "");
        h.checkSignature(adviceDecAround,   "around()", "");
        h.checkSignature(fieldDecIntrD,     "C.intrD", "");
        h.checkSignature(methodDecIntrV,    "C.intrMethV()", "");

    }

}

