
/** @testcase PUREJAVA try requires block JLS 14.19 */
public class TryBlockRequiredCE {
    void method() {
        int f = 0;
        try f = 2; catch(Error e) {}       // CE 6 { expected, catch w/o try
        try int i = 0; catch(Error e) {}   // CE 7 { expected, catch w/o try
        try f++; catch(Error e) {}         // CE 8 { expected, catch w/o try
    }
}
