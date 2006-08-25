import org.aspectj.bridge.AbortException;
import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.IMessageHandler;
import org.aspectj.bridge.IMessage.Kind;

public class MessageHandler implements IMessageHandler {

	public boolean handleMessage(IMessage message) throws AbortException {
		System.out.println(message);
		if (message.getKind() == IMessage.ERROR) {
			System.exit(-1);
		}
		else if (message.getKind() == IMessage.ABORT) {
			throw new AbortException(message.toString());
		}
		return true;
	}

	public boolean isIgnoring(IMessage.Kind kind) {
		return false;
	}

    public void dontIgnore(IMessage.Kind kind) {
    	
    }
	
    public void ignore(IMessage.Kind kind) {
    	
    }
}