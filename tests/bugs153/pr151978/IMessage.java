public interface IMessage {
  void publish();
}


interface IErrorMessage extends IMessage{
  StackTraceElement[] getStackTrace();
}

interface IObjectFactory<E> {
  public <T extends E> T create(Class<T> theObjectType, Object[]
theParameters);
}

class MessageFactory implements IObjectFactory<IMessage>{
  public <T extends IMessage> T create(Class<T> theObjectType, Object[]
theParameters) {
    return null;
  }
}

class Main {
  public static void main(String[] args) {
    IErrorMessage message = new MessageFactory().create(IErrorMessage.class,
new Object[]{"Foo","Bar"});
  }
}
