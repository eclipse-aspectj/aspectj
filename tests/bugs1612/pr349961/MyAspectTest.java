package com.example;

 public class MyAspectTest {

  public static void main(String []argv) {
         A a = new ABean();
         try {
           if (!a.a("aha").equals("aha")) {
		throw new IllegalStateException();
}
throw new IllegalStateException();
             //Assert.assertEquals("aha", a.a("aha"));
             //Assert.fail("Failed due to a weaving problem.");
         }
         catch (Exception e) {
if (!e.getMessage().equals("OK")) {
throw new IllegalStateException();
}
             //Assert.assertEquals("OK", e.getMessage());
         }
     }
 }

