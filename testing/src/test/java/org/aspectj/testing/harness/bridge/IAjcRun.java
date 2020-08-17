/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Xerox/PARC     initial implementation
 * ******************************************************************/

package org.aspectj.testing.harness.bridge;

import org.aspectj.testing.run.IRun;
import org.aspectj.testing.run.IRunStatus;
import org.aspectj.testing.xml.XMLWriter;

// XXX candidate to be subsumed in class/constructors, since inner spec does setup
//     at the same time it constructs the run.
public interface IAjcRun extends IRun {
    boolean setupAjcRun(Sandbox sandbox, Validator validator);
    // XXX add for result eval? ArrayList getExpectedMessages();

    /** this IAjcRun does nothing, returning true always */
	IAjcRun NULLRUN = new IAjcRun() {
        public boolean setupAjcRun(Sandbox sandbox, Validator validator) {
            return true;
        }
        public boolean run(IRunStatus status) {
            if (!status.started()) {
                status.start();
            }
            status.finish(IRunStatus.PASS);
            return true;
        }

        public void writeXml(XMLWriter out) {
            throw new UnsupportedOperationException("unimplemented");
        }
        public String toString() { return "IAjcRun.NULLRUN"; }
    };

}
