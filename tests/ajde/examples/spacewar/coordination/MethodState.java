
package coordination;

import java.util.Vector;
import java.util.Enumeration;


class MethodState {

    Vector threads=new Vector();

    void enterInThread (Thread t) {
	threads.addElement(t);
    }

    void exitInThread(Thread t) {
	threads.removeElement(t);
    }

    boolean hasOtherThreadThan(Thread t) {
	Enumeration e = threads.elements();
	while (e.hasMoreElements())
	    if (e.nextElement() != t)
		return(true);
	return (false);
    }

}
