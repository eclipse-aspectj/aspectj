
import org.aspectj.lang.*;
import org.aspectj.lang.reflect.*;
import java.io.*;

/**
 * Produces an XML representation of all of the join points in a program
 * within the control-flow of those points matching <code>entry</code>.
 *
 * To use this, extend this aspect and fill in an appropriate value for
 * <code>entry</code>.
 */
public abstract aspect TraceJoinPoints dominates * {
    /**
     * The join points which mark the entry of the traced control-flow.
     * To trace all command-line programs, set this to:
     *     <pre>call(public static void main(String[]));</pre>
     */
    protected abstract pointcut entry();

    /**
     * Join points which mark an exit from the control-flow.  Use this
     * to exclude parts of the call-graph that you're not interested in.
     * The default value matches the current implementation limitation
     * (in ajc-1.0) that join points within system libraries are not
     * visible.
     */
    protected pointcut exit(): call(* java..*.*(..));

    final pointcut start(): entry() && !cflowbelow(entry());

    final pointcut trace():
        cflow(entry()) && !cflowbelow(exit()) && !within(TraceJoinPoints+);

    before(): start() { makeLogStream(); }

    before(): trace() { logEnter(thisJoinPointStaticPart); }
    after(): trace() { logExit(thisJoinPointStaticPart); }

    after(): start() { closeLogStream(); }


    PrintStream out;
    int logs = 0;
    protected void makeLogStream() {
        try {
            out = new PrintStream(new FileOutputStream("log" + logs++ + ".xml"));
        } catch (IOException ioe) {
            out = System.err;
        }
    }

    protected void closeLogStream() {
        out.close();
    }


    int depth = 0;
    boolean terminal = false;
    protected void logEnter(JoinPoint.StaticPart jp) {
        if (terminal) out.println(">");
        indent(depth);
        out.print("<" + jp.getKind());
        writeSig(jp);
        writePos(jp);

        depth += 1;
        terminal = true;
    }

    void writeSig(JoinPoint.StaticPart jp) {
        out.print(" sig=");
        out.print(quoteXml(jp.getSignature().toShortString()));
    }

    void writePos(JoinPoint.StaticPart jp) {
        SourceLocation loc = jp.getSourceLocation();
        if (loc == null) return;

        out.print(" pos=");
        out.print(quoteXml(loc.getFileName() +
                           ":" + loc.getLine() +
                           ":" + loc.getColumn()));
    }

    String quoteXml(String s) {
        return "\"" + s.replace('<', '_').replace('>', '_') + "\"";
    }

    protected void logExit(JoinPoint.StaticPart jp) {
        depth -= 1;
        if (terminal) {
            out.println("/>");
        } else {
            indent(depth);
            out.println("</" + jp.getKind() + ">");
        }
        terminal = false;
    }

    void indent(int i) {
        while (i-- > 0) out.print("  ");
    }
}
