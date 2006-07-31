// IOExceptionInjector.aj
package test;

import java.io.IOException;

public aspect IOExceptionInjector {
        pointcut faultInjection() : call(void methodThatThrows() throws
IOException);

        void around() throws IOException : faultInjection() {
                throw new IOException("Exception injected by aspect."); 
        }
}