/*
 * Copyright (c) 1998-2002 PARC Inc.  All rights reserved.
 *
 * Use and copying of this software and preparation of derivative works based
 * upon this software are permitted.  Any distribution of this software or
 * derivative works must comply with all applicable United States export
 * control laws.
 *
 * This software is made available AS IS, and PARC Inc. makes no
 * warranty about the software, its performance or its conformity to any
 * specification.
 */

import java.util.*;
import java.io.*;

import org.aspectj.lang.*;

/** @author Wes Isberg */
public aspect BufferTest {

    // article page 43 - input driver
    // START-SAMPLE testing-inoculated-proceedVariants Using around for integration testing
    /**
     * When PrinterBuffer.capacity(int) is called,
     * test it with repeatedly with a set of input
     * (validating the result) and then continue with
     * the original call.
     *
     * This assumes that the capacity method causes no
     * relevant state changes in the buffer.
     */
    int around(int original, PrinterBuffer buffer) : 
        call(int PrinterBuffer.capacity(int)) && args(original) && target(buffer) {
        int[] input = new int[] { 0, 1, 10, 1000, -1, 4096 };
        for (int i = 0; i < input.length; i++) {
            int result = proceed(input[i], buffer); // invoke test
            validateResult(buffer, input[i], result);
        }
        return proceed(original, buffer);           // continue with original processing
    }
    // END-SAMPLE testing-inoculated-proceedVariants

    void validateResult(PrinterBuffer buffer, int input, int result) {
        System.err.println("validating input=" + input + " result=" + result
                           + " buffer=" + buffer);
    }

    public static void main(String[] args) {
        PrinterBuffer p = new PrinterBuffer();
        int i = p.capacity(0);
        System.err.println("main - result " + i);
    }
} 

class PrinterBuffer {
    int capacity(int i) {
        System.err.println("capacity " + i);
        return i;
    }
}
