
import org.aspectj.testing.*;

/** @testcase PR#691 around AST type XXX */
public class PR691 {
    public static void main (String[] args) {
        Tester.expectEvent("around");
        new MailerTest().run(new TestResult());
        Tester.checkAllEvents();
    } 
    
}
class TestResult {}
class Message {}
class MailerTest { 
    public void run(TestResult result) { 
        new Mailer().sendTextMail();
    }
}
class Mailer { 
    public void sendTextMail(){ 
        new Transport().send(new Message());
    } 
}
class Transport { public void send(Message m){ } }

aspect Mail {
    pointcut inThisTestCase(MailerTest testCase) : 
        call(* MailerTest.run(TestResult)) 
        && target(testCase);

    pointcut flowOfTestCase(MailerTest testCase) : 
        cflow(inThisTestCase(testCase));

    pointcut sendMailCall() : call(void Mailer.sendTextMail(..));
    pointcut transportSend(Message msg) : 
        call(void Transport.send(Message)) && args(msg);
        
    // no bug if no testCase context
    //void around(Message msg) :  
    // flowOfTestCase(MailerTest) 
    void around(Message msg, final MailerTest testCase) :  
        flowOfTestCase(testCase) 
        && cflow(sendMailCall())  
        && transportSend(msg) {
        Tester.event("around");
        proceed(msg,testCase);
    }
}
