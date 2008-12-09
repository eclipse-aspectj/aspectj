import java.util.List;
import java.util.ArrayList;


public class C<E extends Number> {
//      void m1(List<Integer> e){}      
//      void m2(List<? extends Number> e){}
//      void m3(List<Number> e){}       
//      void m4(List<?> e){}
//      void m5(List<E> e){}
//      void m6(List<? extends E> e){}
//      void m7(List<? extends List<? extends E>> e){}
//      void m8(List e){}
//      void m9(E e){}
}

class A1{}
class B1 extends A1{}
class C1 extends B1{}
class D1 extends C1{}

class D2<E2 extends C1>{
        void m5(List<E2> e){}
}

aspect AC{
//void around(): execution(* C.m1(..))  && args(List<Integer>){} //: Should
match (it does)     
//void around(): execution(* C.m1(..))  && args(ArrayList<Integer>){}//: Should
runtime check (it does!)
//void around(): execution(* C.m1(..))  && args(List<Number>){}//: Should not
match (it does not!)      
//void around(): execution(* C.m1(..))  && args(ArrayList<Number>){}//: Should
not match (it does not)
//void around(): execution(* C.m1(..))  && args(List<? extends Number>){}//:
Should match (it does)
//void around(): execution(* C.m1(..))  && args(ArrayList<? extends
Number>){}//: Should runtime check (it does!)
//void around(): execution(* C.m1(..))  && args(List){}//: Should match (it
does)       
//void around(): execution(* C.m1(..))  && args(ArrayList){}//: Should runtime
check (it does not match!)ERROR  
//void around(): execution(* C.m1(..))  && args(List<?>){}//: Should match (it
does)    
//void around(): execution(* C.m1(..))  && args(ArrayList<?>){}//: Should
runtime check (it does not match!)    
//void around(): execution(* C.m1(..))  && args(ArrayList<String>){}//: Should
not match (it does not match!)

//void around(): execution(* C.m2(..))  && args(List<Integer>){} //: Should not
match (but it does) ERROR 
//void around(): execution(* C.m2(..))  && args(ArrayList<Integer>){}//: Should
not match (but it does!) ERROR
//void around(): execution(* C.m2(..))  && args(List<Number>){} //: Should not
match (but it does) ERROR
//void around(): execution(* C.m2(..))  && args(ArrayList<Number>){}//: Should
not runtime check (but it does!) ERROR
//void around(): execution(* C.m2(..))  && args(List<? extends Number>){}//:
Should match (it does)
//void around(): execution(* C.m2(..))  && args(ArrayList<? extends
Number>){}//: Should runtime check (it does!)
//void around(): execution(* C.m2(..))  && args(List){}//: Should match (it
does)       
//void around(): execution(* C.m2(..))  && args(ArrayList){}//: Should runtime
check (it does not match!) ERROR 
//void around(): execution(* C.m2(..))  && args(List<?>){}//: Should match (it
does)    
//void around(): execution(* C.m2(..))  && args(ArrayList<?>){}//: Should
runtime check (it does!)      
//void around(): execution(* C.m2(..))  && args(ArrayList<String>){}//: Should
not match (it does not match!)

//      void around(): execution(* C.m3(..))  && args(List<Integer>){} //:
Should not match (it does not) 
//      void around(): execution(* C.m3(..))  && args(ArrayList<Integer>){}//:
Should not match (it does not)
//      void around(): execution(* C.m3(..))  && args(List<Number>){}//: Should
match (it does) 
//      void around(): execution(* C.m3(..))  && args(ArrayList<Number>){}//:
Should runtime match (it does)
//      void around(): execution(* C.m3(..))  && args(List<? extends
Number>){}//: Should match (it does)
//      void around(): execution(* C.m3(..))  && args(ArrayList<? extends
Number>){}//: Should runtime check (it does!)
//      void around(): execution(* C.m3(..))  && args(List){}//: Should match
(it does) 
//      void around(): execution(* C.m3(..))  && args(ArrayList){}//: Should
runtime check (it does not match!) ERROR   
//      void around(): execution(* C.m3(..))  && args(List<?>){}//: Should
match (it does)      
//      void around(): execution(* C.m3(..))  && args(ArrayList<?>){}//: Should
runtime check (it does!)        
//      void around(): execution(* C.m3(..))  && args(ArrayList<String>){}//:
Should not match (it does not match!)     

//      void around(): execution(* C.m4(..))  && args(List<Integer>){} //:
Should not match (but it does) ERROR
//      void around(): execution(* C.m4(..))  && args(ArrayList<Integer>){}//:
Should not match (but it does) ERROR
//      void around(): execution(* C.m4(..))  && args(List<Number>){}//: Should
not match (but it does) ERROR
//      void around(): execution(* C.m4(..))  && args(ArrayList<Number>){}//:
Should not match (but it does) ERROR
//      void around(): execution(* C.m4(..))  && args(List<? extends
Number>){}//: Should not match (but it does) ERROR
//      void around(): execution(* C.m4(..))  && args(ArrayList<? extends
Number>){}//: Should not match (but it does!) ERROR
//      void around(): execution(* C.m4(..))  && args(List){}//: Should match
(it does) 
//      void around(): execution(* C.m4(..))  && args(ArrayList){}//: Should
runtime check (it does!)   
//      void around(): execution(* C.m4(..))  && args(List<?>){}//: Should
match (it does)      
//      void around(): execution(* C.m4(..))  && args(ArrayList<?>){}//: Should
runtime check (it does!)        
//      void around(): execution(* C.m4(..))  && args(ArrayList<String>){}//:
Should not match (it does not match!)     

//      void around(): execution(* C.m5(..))  && args(List<Integer>){} //:
Should not match (but it does) ERROR 
//      void around(): execution(* C.m5(..))  && args(ArrayList<Integer>){}//:
Should not match (but it does!) ERROR
//      void around(): execution(* C.m5(..))  && args(List<Number>){}//: Should
not match (but it does!) ERROR
//      void around(): execution(* C.m5(..))  && args(ArrayList<Number>){}//:
Should not match (it does) ERROR
//      void around(): execution(* C.m5(..))  && args(List<? extends
Number>){}//: Should match (it does)
//      void around(): execution(* C.m5(..))  && args(ArrayList<? extends
Number>){}//: Should runtime check (it does!)
//      void around(): execution(* C.m5(..))  && args(List){}//: Should match
(it does) 
//      void around(): execution(* C.m5(..))  && args(ArrayList){}//: Should
runtime check (it does not match!) ERROR   
//      void around(): execution(* C.m5(..))  && args(List<?>){}//: Should
match (it does)      
//      void around(): execution(* C.m5(..))  && args(ArrayList<?>){}//: Should
runtime check (it does not match!)      
//      void around(): execution(* C.m5(..))  && args(ArrayList<String>){}//:
Should not match (it does not match!) 

//      void around(): execution(* D2.m5(..))  && args(List<D1>){} //: Should
not match (but it does) ERROR 
//      void around(): execution(* D2.m5(..))  && args(ArrayList<D1>){}//:
Should not match (but it does!) ERROR
//      void around(): execution(* D2.m5(..))  && args(List<C1>){}//: Should
not match (but it does!) ERROR
//      void around(): execution(* D2.m5(..))  && args(ArrayList<C1>){}//:
Should not match (it does) ERROR
//      void around(): execution(* D2.m5(..))  && args(List<? extends B1>){}//:
Should match (it does)
//      void around(): execution(* D2.m5(..))  && args(ArrayList<? extends
B1>){}//: Should runtime check (it does!)
//      void around(): execution(* D2.m5(..))  && args(List<? extends C1>){}//:
Should match (it does)
//      void around(): execution(* D2.m5(..))  && args(ArrayList<? extends
C1>){}//: Should runtime check (it does!)
//      void around(): execution(* D2.m5(..))  && args(List){}//: Should match
(it does)        
//      void around(): execution(* D2.m5(..))  && args(ArrayList){}//: Should
runtime check (it does not match!) ERROR  
//      void around(): execution(* D2.m5(..))  && args(List<?>){}//: Should
match (it does)     
//      void around(): execution(* D2.m5(..))  && args(ArrayList<?>){}//:
Should runtime check (it does not match!)     
//      void around(): execution(* D2.m5(..))  && args(ArrayList<String>){}//:
Should not match (it does not match!)    

//      void around(): execution(* C.m6(..))  && args(List<Integer>){} //:
Should not match (but it does) ERROR 
//      void around(): execution(* C.m6(..))  && args(ArrayList<Integer>){}//:
Should not match (but it does!) ERROR
//      void around(): execution(* C.m6(..))  && args(List<Number>){}//: Should
not match (but it does!) ERROR
//      void around(): execution(* C.m6(..))  && args(ArrayList<Number>){}//:
Should not match (it does) ERROR
//      void around(): execution(* C.m6(..))  && args(List<? extends
Number>){}//: Should match (it does)
//      void around(): execution(* C.m6(..))  && args(ArrayList<? extends
Number>){}//: Should runtime check (it does!)
//      void around(): execution(* C.m6(..))  && args(List){}//: Should match
(it does) 
//      void around(): execution(* C.m6(..))  && args(ArrayList){}//: Should
runtime check (it does not match!) 
//      void around(): execution(* C.m6(..))  && args(List<?>){}//: Should
match (it does)      
//      void around(): execution(* C.m6(..))  && args(ArrayList<?>){}//: Should
runtime check (it does not match!)      
//      void around(): execution(* C.m6(..))  && args(ArrayList<String>){}//:
Should not match (it does not match!)             

//      void around(): execution(* C.m7(..))  && args(List<List<Integer>>){}
//: Should not match (but it does) ERROR 
//      void around(): execution(* C.m7(..))  &&
args(ArrayList<List<Integer>>){}//: Should not match (but it does!) ERROR
//      void around(): execution(* C.m7(..))  && args(List<List<Number>>){}//:
Should not match (but it does!) ERROR
//      void around(): execution(* C.m7(..))  &&
args(ArrayList<List<Number>>){}//: Should not match (but it does) ERROR
//      void around(): execution(* C.m7(..))  && args(List<? extends
List<Number>>){}//: Should not match (but it does) ERROR
//      void around(): execution(* C.m7(..))  && args(ArrayList< ? extends
List<Number>>){}//: Should not match (but it does!) ERROR
//      void around(): execution(* C.m7(..))  && args(List< ? extends List<?
extends Number>>){}//: Should match (it does!)     
//      void around(): execution(* C.m7(..))  && args(ArrayList< ? extends
List<? extends Number>>){}//: Should match (it does!)
//      void around(): execution(* C.m7(..))  && args(List){}//: Should match
(it does) 
//      void around(): execution(* C.m7(..))  && args(ArrayList){}//: Should
runtime check (it does not match!) 
//      void around(): execution(* C.m7(..))  && args(List<?>){}//: Should
match (it does)      
//      void around(): execution(* C.m7(..))  && args(ArrayList<?>){}//: Should
runtime check (it does!)        
//      void around(): execution(* C.m7(..))  &&
args(ArrayList<List<String>>){}//: Should not match (it does not match!)       

//      void around(): execution(* C.m8(..))  && args(List<Integer>){} //:
Should match with unchecked conversion (it does) 
//      void around(): execution(* C.m8(..))  && args(ArrayList<Integer>){}//:
Should runtime check with unchecked conversion (it does!)
//      void around(): execution(* C.m8(..))  && args(List<Number>){}//: Should
match with unchecked conversion (it does!)      
//      void around(): execution(* C.m8(..))  && args(ArrayList<Number>){}//:
Should runtime check with unchecked conversion (it does)
//      void around(): execution(* C.m8(..))  && args(List<? extends
Number>){}//: Should match with unchecked conversion (it does!)    
//      void around(): execution(* C.m8(..))  && args(ArrayList<? extends
Number>){}//: Should runtime check with unchecked conversion (it does)
//      void around(): execution(* C.m8(..))  && args(List){}//: Should match
(it does) 
//      void around(): execution(* C.m8(..))  && args(ArrayList){}//: Should
runtime check (it does!)   
//      void around(): execution(* C.m8(..))  && args(List<?>){}//: Should
match (it does)      
//      void around(): execution(* C.m8(..))  && args(ArrayList<?>){}//: Should
runtime check (it does!)        
//      void around(): execution(* C.m8(..))  && args(ArrayList<String>){}//:
Should not match (it does not match!)     

//      void around(): execution(* C.m9(..))  && args(List<Integer>){} //:
Should not match (but it does) ERROR 
//      void around(): execution(* C.m9(..))  && args(ArrayList<Integer>){}//:
Should not match (it does not match!)
//      void around(): execution(* C.m9(..))  && args(Number){}//: Should match
(it does!)      
//      void around(): execution(* C.m9(..))  && args(Integer){}//: Should
runtime check (it does)
//      void around(): execution(* C.m9(..))  && args(List<? extends
Number>){}//: Should not match (but it does) ERROR
//      void around(): execution(* C.m9(..))  && args(ArrayList<? extends
Number>){}//: Should not match (it does not match!)
//      void around(): execution(* C.m9(..))  && args(List){}//: Should not
match (but it does) ERROR   
//      void around(): execution(* C.m9(..))  && args(ArrayList){}//: Should
not match (it does not match!)     
//      void around(): execution(* C.m9(..))  && args(List<?>){}//: Should not
match (but it does) ERROR        
//      void around(): execution(* C.m9(..))  && args(ArrayList<?>){}//: Should
not match (it does not match!)  
//      void around(): execution(* C.m9(..))  && args(String){}//: Should not
match (it does not match!)        
}

