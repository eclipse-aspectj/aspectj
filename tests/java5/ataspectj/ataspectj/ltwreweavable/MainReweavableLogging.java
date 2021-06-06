/*******************************************************************************
 * Copyright (c) 2005 Contributors.
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *
 * Contributors:
 *   Alexandre Vasseur         initial implementation
 *******************************************************************************/
package ataspectj.ltwreweavable;

import ataspectj.ltwlog.MessageHolder;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Contributed by David Knibb
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
public class MainReweavableLogging implements Advisable {
    private static List joinPoints = new ArrayList();

    public void test1 () {

    }

    public void test2 () {

    }

    public void addJoinPoint (String name) {
        joinPoints.add(name);
    }

    public static void main (String[] args) {
        String ERROR_STRING = "error aspect 'ataspectj.ltwreweavable.AspectReweavableLogging' woven into 'ataspectj.ltwreweavable.MainReweavableLogging' must be defined to the weaver (placed on the aspectpath, or defined in an aop.xml file if using LTW).";
        if(Boolean.getBoolean("aspectDeclared")){
            //if the aspect is declared there should not be an error
            if (MessageHolder.startsAs( Arrays.asList(  new String[]{ ERROR_STRING }  )) ) {
                MessageHolder.dump();
                throw new RuntimeException("Error in MainReweavableLogging - unexpected error message - \"" + ERROR_STRING + "\"");
            }
        }
        else{
            //and if the aspect is not declared then there should
            if (!MessageHolder.startsAs( Arrays.asList(  new String[]{ ERROR_STRING }  )) ) {
                MessageHolder.dump();
                throw new RuntimeException("Error in MainReweavableLogging - missing expected error message - \"" + ERROR_STRING + "\"");
            }
        }
    }
}
