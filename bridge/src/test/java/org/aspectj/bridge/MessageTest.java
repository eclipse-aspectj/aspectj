/* *******************************************************************
 * Copyright (c) 1999-2001 Xerox Corporation, 
 *               2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Xerox/PARC     initial implementation 
 * ******************************************************************/

package org.aspectj.bridge;

import java.io.File;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;
import junit.textui.TestRunner;

/**
 * 
 */
public class MessageTest extends TestCase {

   private static final String ME 
        = "org.aspectj.bridge.MessageTest"; // XXX

    /** @param args ignored */
    public static void main(String[] args) {
        TestRunner.main(new String[] {ME});
    }

	/**
	 * Constructor for MessageTest.
	 * @param name
	 */
	public MessageTest(String name) {
		super(name);
	}
    
    <T> void checkListOrder(List<T> list, Comparator<T> c) { // XXX util
        assertNotNull(list);
        assertNotNull(c);
        ListIterator<T> it = list.listIterator();
        T last = null;
        T current = null;
        while (it.hasNext()) {
            current = it.next();
            if (null != last) {
                int i = c.compare(last, current);
                if (i > 0) {
                    assertTrue( last + " > " + current + " (" + i + ")", false);
                }                
            } 
            last = current;
        }
    }
        
    public void testKindOrder() {
        // first briefly validate the checker
        checkListOrder(Arrays.asList(new String[] { "a", "b", "C" }),
            String.CASE_INSENSITIVE_ORDER);
        checkListOrder(IMessage.KINDS, IMessage.Kind.COMPARATOR);
    }
    
    public void testKind_isSameOrLessThan() {
        IMessage.Kind last;
        IMessage.Kind next = null;
		for (IMessage.Kind kind : IMessage.KINDS) {
			last = next;
			next = kind;
			if (null == last) {
				continue;
			}
			String label = "last: " + last + " next: " + next;
			assertTrue(label, !next.isSameOrLessThan(null));
			assertTrue(label, !next.isSameOrLessThan(last));
			assertTrue(label, last.isSameOrLessThan(next));
			assertTrue(label, next.isSameOrLessThan(next));
		}
    }
    
    public void testMessageHandler() {
        boolean handleMessageResult = true;
        checkEmptyMessageHolder(new MessageHandler(handleMessageResult), handleMessageResult);
        handleMessageResult = false;
        checkEmptyMessageHolder(new MessageHandler(handleMessageResult), handleMessageResult);
    }
    
    public void checkEmptyMessageHolder(
        IMessageHolder h, 
        final boolean handleMessageResult) {
       //  { INFO, DEBUG, WARNING, ERROR, FAIL, ABORT }));  
        assertNotNull(h);
        assertTrue(!h.hasAnyMessage(null, true));
        assertTrue(!h.hasAnyMessage(null, false));
        assertTrue(!h.hasAnyMessage(IMessage.INFO, true));
        assertTrue(!h.hasAnyMessage(IMessage.INFO, false));
        
        assertTrue(handleMessageResult == h.handleMessage(make("error 1", IMessage.ERROR)));
        assertTrue(handleMessageResult == h.handleMessage(make("error 2", IMessage.ERROR)));
        assertTrue(handleMessageResult == h.handleMessage(make("fail 1", IMessage.FAIL)));
        assertTrue(handleMessageResult == h.handleMessage(make("fail 2", IMessage.FAIL)));
        assertTrue(handleMessageResult == h.handleMessage(make("debug 1", IMessage.DEBUG)));
        assertTrue(handleMessageResult == h.handleMessage(make("debug 2", IMessage.DEBUG)));
        
        assertTrue(h.hasAnyMessage(null, true));
        assertTrue(h.hasAnyMessage(null, false));
        assertTrue(h.hasAnyMessage(IMessage.ERROR, true));
        assertTrue(h.hasAnyMessage(IMessage.ERROR, false));
        assertTrue(h.hasAnyMessage(IMessage.FAIL, true));
        assertTrue(h.hasAnyMessage(IMessage.FAIL, false));
        assertTrue(h.hasAnyMessage(IMessage.DEBUG, true));
        assertTrue(h.hasAnyMessage(IMessage.DEBUG, false));
        
        assertTrue(!h.hasAnyMessage(IMessage.INFO, IMessageHolder.EQUAL));
        assertTrue(!h.hasAnyMessage(IMessage.WARNING, IMessageHolder.EQUAL));
        assertTrue(!h.hasAnyMessage(IMessage.ABORT, IMessageHolder.EQUAL));
        assertTrue(h.hasAnyMessage(IMessage.INFO, IMessageHolder.ORGREATER));
        assertTrue(h.hasAnyMessage(IMessage.WARNING, IMessageHolder.ORGREATER));
        assertTrue(!h.hasAnyMessage(IMessage.ABORT, IMessageHolder.ORGREATER));
        
        assertTrue(0 == h.numMessages(IMessage.INFO, IMessageHolder.EQUAL));
        assertTrue(0 == h.numMessages(IMessage.WARNING, IMessageHolder.EQUAL));
        assertTrue(0 == h.numMessages(IMessage.ABORT, IMessageHolder.EQUAL));

        assertTrue(6 == h.numMessages(null, IMessageHolder.ORGREATER));
        assertTrue(6 == h.numMessages(null, IMessageHolder.EQUAL));
        assertTrue(6 == h.numMessages(IMessage.INFO, IMessageHolder.ORGREATER));
        assertTrue(6 == h.numMessages(IMessage.DEBUG, IMessageHolder.ORGREATER));
        assertTrue(4 == h.numMessages(IMessage.WARNING, IMessageHolder.ORGREATER));
        assertTrue(4 == h.numMessages(IMessage.ERROR, IMessageHolder.ORGREATER));
        assertTrue(2 == h.numMessages(IMessage.FAIL, IMessageHolder.ORGREATER));
        assertTrue(0 == h.numMessages(IMessage.ABORT, IMessageHolder.ORGREATER));
        
    } 
    
    public void testMessage() {
        String input = "input";
        Throwable thrown = null;
        ISourceLocation sl = null;
        Class<?> exClass = null;
        String descriptor = "Message"; // for make(...)
        IMessage.Kind kind = IMessage.INFO;

        // -- kind variants
        roundTrip(input, kind, thrown, sl, descriptor, exClass);
        kind = IMessage.WARNING;
        roundTrip(input, kind, thrown, sl, descriptor, exClass);
        kind = IMessage.ERROR;
        roundTrip(input, kind, thrown, sl, descriptor, exClass);
        kind = IMessage.DEBUG;
        roundTrip(input, kind, thrown, sl, descriptor, exClass);
        kind = IMessage.FAIL;
        roundTrip(input, kind, thrown, sl, descriptor, exClass);

        // -- throwable
        kind = IMessage.FAIL;
        thrown = new AbortException();
        input = null;
        roundTrip(input, kind, thrown, sl, descriptor, exClass);

        // -- source location
        kind = IMessage.WARNING;
        thrown = null;
        input = "type not found";
        File f = new File("some/file.java"); // XXX unchecked
        sl = new SourceLocation(f, 0, 0, 0);
        roundTrip(input, kind, thrown, sl, descriptor, exClass);
        sl = new SourceLocation(f, 1, 1, 0);
        roundTrip(input, kind, thrown, sl, descriptor, exClass);

        // -- input error tests - null kind, null input (factory-dependent)
        kind = null;
        exClass = IllegalArgumentException.class;
        roundTrip(input, kind, thrown, sl, descriptor, exClass);
        input = null;
        kind = IMessage.INFO;
        roundTrip(input, kind, thrown, sl, descriptor, exClass);                        
    }
    
    protected IMessage make(String message, IMessage.Kind kind) {
        return new Message(message, kind, null, null);
    }

    /** make a Message per descriptor and input */
    protected IMessage make(String input, IMessage.Kind kind,
        Throwable thrown, ISourceLocation sourceLocation, 
        String descriptor) { // XXX ignored for now
            return new Message(input, kind, thrown, sourceLocation);
    }

    /**
     * Simple round-trip on the message
     */
    protected void roundTrip(String input, IMessage.Kind kind,
        Throwable thrown, ISourceLocation sourceLocation, 
        String descriptor, Class<?> exClass) {
        try {
            IMessage m = make(input, kind, thrown, sourceLocation, descriptor);
            if ((null == input) && (null != thrown)) {
                input = thrown.getMessage();
            }
            roundTripCheck(m, input, kind, thrown, sourceLocation);
        } catch (AssertionFailedError x) {
            throw x;
        } catch (Throwable t) {
            assertTrue(null != exClass);
            assertTrue(exClass.isAssignableFrom(t.getClass()));
        }
    }

    protected void roundTripCheck(IMessage message, String input, IMessage.Kind kind,
        Throwable thrown, ISourceLocation sourceLocation) {
            IMessage m = message;
            assertTrue("not null", null != m);
            assertEquals(input, m.getMessage());
            assertTrue(""+kind, kind == m.getKind());
            assertTrue(""+thrown, equals(thrown, m.getThrown()));
            assertTrue(""+sourceLocation, 
                equals(sourceLocation, m.getSourceLocation()));
            String err = new KindTest().testKindSet(message, kind);
            if (null != err) {
                assertTrue(err, false);
            }
    }
    
    protected static boolean equals(Object one, Object two) {
        if (null == one) {
            return (null == two);
        } else if (null == two) {
            return false;
        } else {
            return one.equals(two);
        }
    }
}

/** test correlation between message and enclosed kind */
class KindTest {
    /** order tracked in checkKindMethods() */
    static final IMessage.Kind[] KINDS = new IMessage.Kind[] 
            {  IMessage.ABORT, IMessage.DEBUG, IMessage.ERROR, 
                IMessage.INFO, IMessage.WARNING, IMessage.FAIL };

    static final List<IMessage.Kind> KINDLIST = Arrays.asList(KINDS);

    /** used to clear instance BitSet */
    static final BitSet UNSET = new BitSet(KINDS.length);


    final BitSet expected = new BitSet(KINDS.length);
    IMessage.Kind kind = IMessage.INFO;
    
    /** @return error if failed */
    public String testKindSet(IMessage m, IMessage.Kind newKind) {
        IMessage.Kind oldKind = this.kind;
        String result = setKind(newKind);
        if (null == result) {
            result = checkKindSet(m, newKind);
        }
        if (null == result) {
            result = checkExpectedKind(m);
        }
        return (null != result? result : setKind(oldKind));
    }
    
    /** @return error if failed */
    private String setKind(IMessage.Kind kind) {
        this.kind = kind;
        int index = KINDLIST.indexOf(kind);
        if (-1 == index) {
            return "unknown kind: " + kind;
        }
        expected.and(UNSET);
        expected.set(index);
        return null;
    }
    
    /** @return error if failed */
    String checkExpectedKind(IMessage m) {
        StringBuffer result = new StringBuffer();
        for (int i = 0; i < KINDS.length; i++) {
            if (expected.get(i) != checkKindMethods(m, i)) {
                String s = "expected " + expected.get(i)
                    + " for is{Method} for " + KINDS[i];
                result.append(s + "\n");
            } 
		}  
        return (0 < result.length() ? result.toString() : null); 
    }
    
    String checkKindSet(IMessage m, IMessage.Kind kind) {
        if (kind != m.getKind()) {
            return "expected kind " + kind + " got " + m.getKind();
        }
        return null;
    }
    
    /** @return true if index matches isFoo() reporting */
    boolean checkKindMethods(IMessage m, int index) {
        switch (index) {
            case (0) : return m.isAbort(); 
            case (1) : return m.isDebug(); 
            case (2) : return m.isError(); 
            case (3) : return m.isInfo(); 
            case (4) : return m.isWarning(); 
            case (5) : return m.isFailed(); 
            default : throw new IllegalArgumentException("index=" + index);
                                
        }
    }
}
