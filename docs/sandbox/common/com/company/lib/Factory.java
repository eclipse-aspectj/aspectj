
package com.company.lib;

public class Factory {

    public static Thread makeThread(Runnable runnable, String name) {
        class MyThread extends Thread {
            MyThread(Runnable runnable, String name) {
                super(runnable, name);
            }
        }
        return new MyThread(runnable, name);
    }
}