/* -*- Mode: Java; -*-

Copyright (c) Xerox Corporation 1998-2002.  All rights reserved.

Use and copying of this software and preparation of derivative works based
upon this software are permitted.  Any distribution of this software or
derivative works must comply with all applicable United States export control
laws.
 
This software is made available AS IS, and Xerox Corporation makes no warranty
about the software, its performance or its conformity to any specification.

|<---            this code is formatted to fit into 80 columns             --->|
|<---            this code is formatted to fit into 80 columns             --->|
|<---            this code is formatted to fit into 80 columns             --->|

*/

package coordination;


/**
 * Interface for coordination actions that are passed to guardedEntry methods of
 * Coordinator.
 * Coordination actions should be passed as anonymous classes that simply 
 * implement the doit method. 
 *
 */
public interface CoordinationAction {
    /**
     * This method is called  by Coordinator.guardedEntry(...) and
     * Coordinator.guardedExit(...). Use it for changing coordination state
     * upon entering and exiting methods.
     */

    public void doit();
}
