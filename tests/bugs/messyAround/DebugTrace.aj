import aspects.*;
//import org.apache.log4j.*;
//import com.checkfree.common.util.*;
import java.lang.reflect.*;
import java.util.*;
import org.aspectj.lang.reflect.*;
/**
* This concrete trace aspect specifies what we should trace.
 */

privileged aspect DebugTrace extends Trace
                                 
{
    declare precedence: DebugTrace, *;
    
    //private static Logger _log = null;
    
    static
    {
        //String log4jPath = GlobalPaths.getPath("properties_dir")+"log4j.properties";
        //PropertyConfigurator.configure(log4jPath);
        //_log = Logger.getLogger(TestLog.class);    
    }
        
    /** define the pointcut for what we trace */
    protected pointcut lexicalScope() :within(cap.OptionList);
        
    protected void log(String data)
    {
        System.err.println("data: " + data);
        //_log.debug(data);        
    }  
    
}
