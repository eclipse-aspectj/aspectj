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
 * Interface for pre-conditions that are passed to guardedEntry methods of
 * Coordinator.
 * Conditions should be passed as anonymous classes that simply implement
 * the checkit method.
 *
 */
public interface Condition {

    /**
     * This method is called automatically by Coordinator.guardedEntry(...)
     * and it's called everytime the coordination state changes.
     */

    public boolean checkit();
}
