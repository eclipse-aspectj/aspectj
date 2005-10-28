/*******************************************************************************
 * Copyright (c) 2005 Contributors.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://eclipse.org/legal/epl-v10.html 
 * 
 * Contributors:
 *   Alexandre Vasseur         initial implementation
 *******************************************************************************/
package ataspectj.hierarchy;

import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;

import java.net.URL;
import java.net.URLClassLoader;
import java.io.File;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.ProceedingJoinPoint;
import ataspectj.TestHelper;

/**
 * Assumes ataspect.hierarchy.app DOES NOT EXISTS in system classpath
 * but exists in 2 folder located at "/app_1" and /app_2
 * ie "/app_1/ataspect.hierarchy.app.*.class"
 *
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
public class AppContainerTest extends TestCase {

    public static interface IApp {

        String invoke(String input);
    }


    IApp app1;
    IApp app2;

    public void setUp() throws Exception {
        try {
            Class k = Class.forName("ataspectj.hierarchy.app.SubApp");
            throw new Exception("config error, app must not be in system classpath");
        } catch (ClassNotFoundException e) {
            ;//fine
        }

        //build path and app
        URL path = AppContainerTest.class.getProtectionDomain().getCodeSource().getLocation();
        String path1 = path.toString() + "app_1/";
        String path2 = path.toString() + "app_2/";

        URLClassLoader app1CL = new URLClassLoader(
                new URL[]{new URL(path1)},
                AppContainerTest.class.getClassLoader()
        );
        URLClassLoader app2CL = new URLClassLoader(
                new URL[]{new URL(path2)},
                AppContainerTest.class.getClassLoader()
        );

        app1 = (IApp)Class.forName("ataspectj.hierarchy.app.SubApp", false, app1CL).newInstance();
        app2 = (IApp)Class.forName("ataspectj.hierarchy.app.SubApp", false, app2CL).newInstance();
    }

    public void testApp1LocalAspect() {
        String res = app1.invoke("app1");
        assertEquals("globalAspect[localAspect[app1]]", res);
    }

    public void testApp2NoLocalAspect() {
        String res = app2.invoke("app2");
        assertEquals("globalAspect[app2]", res);
    }


    @Aspect
    public static class BaseAspect {

        @Around("execution(* ataspectj.hierarchy.app.SubApp.invoke(..))")//TODO IApp
        public Object around(ProceedingJoinPoint jp) throws Throwable {
            String out = (String) jp.proceed();
            return "globalAspect[" + out + "]";
        }
    }

    public static void main(String[] args) {
        TestHelper.runAndThrowOnFailure(suite());
    }

    public static Test suite() {
        return new TestSuite(AppContainerTest.class);
    }


}
