
package org.smart.app;

public class Main {

    public String persistMe;
    
    public static void main(String[] args) {
        Main me = new Main();
        me.persistMe = Util.utility("persistMe shouting!");
    }
}