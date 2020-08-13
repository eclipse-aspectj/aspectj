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

package org.aspectj.internal.tools.ant.taskdefs;

import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

@SuppressWarnings("deprecation")
public abstract class ConditionalTask extends Task {

    public final static String TRUE = "true";

    private   List<If> ifs;
    protected List<If> ifs() {
        return ifs != null ? ifs : (ifs = new Vector<>());
    }

    public If createIf() {
        If i = new If();
        ifs().add(i);
        return i;
    }

    public If createIf(String name, String equals, boolean strict) {
        If i = createIf();
        i.setName(name);
        i.setEquals(equals);
        i.setStrict(strict);
        return i;
    }

    public If createIf(String name, String equals) {
        return createIf(name, equals, false);
    }

    public If createIf(String name) {
        return createIf(name, TRUE, false);
    }

    public If createIf(String name, boolean strict) {
        return createIf(name, TRUE, strict);
    }

    public void setIfs(String ifs) {
        StringTokenizer tok = new StringTokenizer(ifs, ",;: ", false);
        while (tok.hasMoreTokens()) {
            String next = tok.nextToken();
            int iequals = next.lastIndexOf("=");
            String equals;
            String name;
            boolean strict;
            If i = createIf();
            if (iequals != -1) {
                name   = next.substring(0, iequals);
                equals = next.substring(iequals + 1);
                strict = true;
            } else {
                name   = next.substring(0);
                equals = TRUE;
                strict = false;
            }
            i.setName(name);
            i.setEquals(equals);
            i.setStrict(strict);
        }
    }

    public void setIf(String ifStr) {
        setIfs(ifStr);
    }

    public class If {
        public If() {
            this(null, null);
        }
        public If(String name) {
            this(name, TRUE);
        }
        public If(String name, String equals) {
            setName(name);
            setEquals(equals);
        }
        private String name;
        public void setName(String name) {
            this.name = name;
        }
        public String getName() {
            return name;
        }
        private String equals;
        public void setEquals(String equals) {
            this.equals = equals;
        }
        public String getEquals() {
            return equals;
        }
        private boolean strict = false;
        public void setStrict(boolean strict) {
            this.strict = strict;
        }
        public boolean isStrict() {
            return strict;
        }
        public boolean isOk(String prop) {
            return isOk(prop, isStrict());
        }
        //XXX Need a better boolean parser
        public boolean isOk(String prop, boolean isStrict) {
            if (isStrict) {
                return prop != null && prop.equals(getEquals());
            } else {
                if (isOk(prop, true)) {
                    return true;
                }
                if (prop == null || isFalse(getEquals())) {
                    return true;
                }
                if ( (isTrue(getEquals()) && isTrue(prop)) ||
                     (isFalse(getEquals()) && isFalse(prop)) ) {
                    return true;
                }
                return false;
            }
        }
        private boolean isFalse(String prop) {
            return isOneOf(prop, falses) || isOneOf(prop, complement(trues));
        }
        private boolean isTrue(String prop) {
            return isOneOf(prop, trues) || isOneOf(prop, complement(falses));
        }
        private boolean isOneOf(String prop, String[] strings) {
			for (String string : strings) {
				if (string.equals(prop)) {
					return true;
				}
			}
            return false;
        }
        private String[] complement(String[] strings) {
            for (int i = 0; i < strings.length; i++) {
                strings[i] = "!" + strings[i];
            }
            return strings;
        }
    }

    final static String[] falses = { "false", "no"  };
    final static String[] trues  = { "true",  "yes" };

    protected boolean checkIfs() {
        return getFalses().size() == 0;
    }

    protected List<String> getFalses() {
        Iterator<If> iter = ifs().iterator();
        List<String> result = new Vector<>();
        while (iter.hasNext()) {
            If next = iter.next();
            String name = next.getName();
            String prop = project.getProperty(name);
            if (prop == null) {
                prop = project.getUserProperty(name);
            }
            if (!next.isOk(prop)) {
                result.add(name);
            }
        }
        return result;
    }

    public abstract void execute() throws BuildException;
}
