/*
Copyright (c) 2002 Palo Alto Research Center Incorporated. All Rights Reserved.
 */

public class Log {
    private static StringBuffer data = new StringBuffer();

    public static void traceObject(Object o) {
        throw new UnsupportedOperationException();
    }

    public static void log(String s) {
        data.append(s);
        data.append(';');
    }

    public static void logClassName(Class _class) {
        String name = _class.getName();
        int dot = name.lastIndexOf('.');
        if (dot == -1) {
            log(name);
        } else {
            log(name.substring(dot+1, name.length()));
        }
    }

    public static String getString() {
        return data.toString();
    }

    public static void clear() {
        data.setLength(0);
    }
}
