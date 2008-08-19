public class PayloadClass<Type extends Object> {
 private Type payload;

 public void setPayload(Type payload) {
  this.payload = payload;
 }

 public Type getPayload() {
  return this.payload;
 }

 public void run() {
  System.out.println("payload class run");
 }
}

