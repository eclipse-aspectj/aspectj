import java.util.Map;

import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

@Aspect
//@Goo("abc")
@SuppressWarnings("unchecked")
public class DeliveryCallbackInterceptor {
	@Pointcut("execution(boolean org.springframework.integration.message.MessageHandler+.handleMessage(Message))&& args(message)")
	public void handleMethod(Message message) {
	}

	@AfterThrowing(pointcut = "handleMethod(message)", throwing = "e")
	public void invokeDeliveryCallback(Message message, Throwable e) {
		((DeliveryFailureCallback) message.getHeaders().get("errorcallback")).onDeliveryFailed(message, e);
	}
}

class DeliveryFailureCallback {
	public void onDeliveryFailed(Object o, Object p) {
	}
}

class Message {
	public Map<String, DeliveryFailureCallback> getHeaders() {
		return null;
	}
}

@interface Goo { String value(); }
