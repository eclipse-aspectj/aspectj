import org.aspectj.testing.Tester; 

/** @testcase for PR520 - NAB? */
public class PR520 {
    public static void main(String[] args) {
        PR520 me = new PR520();
        me.testValidThreeArgumentCall() ;
        me.testValidThreeArgumentCallTwo(); 
        Tester.checkAllEvents();
    }

    public void testValidThreeArgumentCall() {
        for (int i = 0; i < Logger.PRIORITIES.length; i++) {
            Logger.log(Logger.PRIORITIES[i], 
                       Logger.API, "context=word_" + i);
        }
    }

    public void testValidThreeArgumentCallTwo() {
        Logger.log( Logger.EXCEPTION, Logger.API, "context=EXCEPTION" );
        Logger.log( Logger.DEBUG, Logger.API, "context=DEBUG" );
        Logger.log( Logger.DEBUG, Logger.API, "context=DEBUG-Exception", 
                    new Exception( "bad bad boy" ) );
    }
    public static void signal(String s, String context) {
        signal(context + ": " + s);
    }
    public static void signal(String s) {
        System.err.println(s);
        Tester.event(s);
    }
    private static final String[] EXPECTED;
    static {
        EXPECTED = new String[] 
        { "context=word_0"
          , "call(void Logger.log(Logger.ChromePriority, Unknown, String))"
          , "context=word_1"
          , "call(void Logger.log(Logger.ChromePriority, Unknown, String))"
          , "context=word_2"
          , "call(void Logger.log(Logger.ChromePriority, Unknown, String))"
          , "context=word_3"
          , "call(void Logger.log(Logger.ChromePriority, Unknown, String))"
          , "context=word_4"
          , "call(void Logger.log(Logger.ChromePriority, Unknown, String))"
          , "context=EXCEPTION"
          , "call(void Logger.log(Logger.ChromePriority, Unknown, String))"
          , "context=DEBUG"
          , "call(void Logger.log(Logger.ChromePriority, Unknown, String))"
          , "context=DEBUG-Exception"
          , "call(void Logger.log(Logger.ChromePriority, Unknown, String, Exception))"
        };
        Tester.expectEventsInString(EXPECTED);
    }
}

class Unknown { }
class Logger {
    static class ChromePriority { }
    public static final ChromePriority DEBUG;
    public static final ChromePriority EXCEPTION;
    public static final ChromePriority FATAL;
    public static final ChromePriority INFO;
    public static final ChromePriority WARN;
    public static final Unknown API;
    public static final Logger.ChromePriority[] PRIORITIES; 

    static {
        API = new Unknown();
        PRIORITIES = new Logger.ChromePriority[ 5 ];
        DEBUG = new ChromePriority();
        EXCEPTION = new ChromePriority();
        FATAL = new ChromePriority();
        INFO = new ChromePriority();
        WARN = new ChromePriority();
        PRIORITIES[ 0 ] = Logger.DEBUG;
        PRIORITIES[ 1 ] = Logger.EXCEPTION;
        PRIORITIES[ 2 ] = Logger.FATAL;
        PRIORITIES[ 3 ] = Logger.INFO;
        PRIORITIES[ 4 ] = Logger.WARN;
    }
    public static void log(ChromePriority p, Unknown q, 
                           String message, Exception e) {
        PR520.signal(message);
    }
    public static void log(ChromePriority p, Unknown q, 
                           String message) {
        PR520.signal(message);
    }
}

aspect LoggerCategoryCreator {

    pointcut allLoggingCalls()
        : call(public void Logger.log(..));

    void around(): allLoggingCalls() {
        // s.b. no proceed() (i.e., replace) but testing invocation
        proceed();
        PR520.signal(thisJoinPointStaticPart.toString());
    }
}
