import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class Investigation {
	public static void main(String[] args) {
		
	}
	
	
	// Basic synchronized method
	public void a() {
		synchronized (this) {
			
		}
	}
	
	// ... that does something ...
	public void b() {
		synchronized (this) {
			System.out.println("hello");
		}
	}
	
	// ... that includes try/catch ...
	public void c() {
		synchronized(this) {
			try {
				File f = new File("fred");
				FileInputStream fis = new FileInputStream(f);
			} catch (IOException ioe) {
				System.out.println("bang");
			}
		}
	}
	
	// ... with multiple synchronized blocks ...
	public void d() {
		synchronized (this) {
			System.out.println("hello");
		}
		synchronized (this) {
			System.out.println("world");
		}
	}
	
	// ... with nested synchronized blocks ...
	public void e() {
		synchronized (this) {
			System.out.println("hello");
			synchronized (new String()) {
				System.out.println("other");
			}
		}
	}
	
	/*
	Compiled from "Investigation.java"
	public class Investigation extends java.lang.Object
	  SourceFile: "Investigation.java"
	  minor version: 0
	  major version: 46
	  Constant pool:
	const #1 = Asciz	Investigation;
	const #2 = class	#1;	//  Investigation
	const #3 = Asciz	java/lang/Object;
	const #4 = class	#3;	//  java/lang/Object
	const #5 = Asciz	<init>;
	const #6 = Asciz	()V;
	const #7 = Asciz	Code;
	const #8 = NameAndType	#5:#6;//  "<init>":()V
	const #9 = Method	#4.#8;	//  java/lang/Object."<init>":()V
	const #10 = Asciz	LineNumberTable;
	const #11 = Asciz	LocalVariableTable;
	const #12 = Asciz	this;
	const #13 = Asciz	LInvestigation;;
	const #14 = Asciz	main;
	const #15 = Asciz	([Ljava/lang/String;)V;
	const #16 = Asciz	org.aspectj.weaver.MethodDeclarationLineNumber;
	const #17 = Asciz	args;
	const #18 = Asciz	[Ljava/lang/String;;
	const #19 = Asciz	a;
	const #20 = Asciz	b;
	const #21 = Asciz	java/lang/System;
	const #22 = class	#21;	//  java/lang/System
	const #23 = Asciz	out;
	const #24 = Asciz	Ljava/io/PrintStream;;
	const #25 = NameAndType	#23:#24;//  out:Ljava/io/PrintStream;
	const #26 = Field	#22.#25;	//  java/lang/System.out:Ljava/io/PrintStream;
	const #27 = Asciz	hello;
	const #28 = String	#27;	//  hello
	const #29 = Asciz	java/io/PrintStream;
	const #30 = class	#29;	//  java/io/PrintStream
	const #31 = Asciz	println;
	const #32 = Asciz	(Ljava/lang/String;)V;
	const #33 = NameAndType	#31:#32;//  println:(Ljava/lang/String;)V
	const #34 = Method	#30.#33;	//  java/io/PrintStream.println:(Ljava/lang/String;)V
	const #35 = Asciz	c;
	const #36 = Asciz	java/io/File;
	const #37 = class	#36;	//  java/io/File
	const #38 = Asciz	fred;
	const #39 = String	#38;	//  fred
	const #40 = NameAndType	#5:#32;//  "<init>":(Ljava/lang/String;)V
	const #41 = Method	#37.#40;	//  java/io/File."<init>":(Ljava/lang/String;)V
	const #42 = Asciz	java/io/FileInputStream;
	const #43 = class	#42;	//  java/io/FileInputStream
	const #44 = Asciz	(Ljava/io/File;)V;
	const #45 = NameAndType	#5:#44;//  "<init>":(Ljava/io/File;)V
	const #46 = Method	#43.#45;	//  java/io/FileInputStream."<init>":(Ljava/io/File;)V
	const #47 = Asciz	bang;
	const #48 = String	#47;	//  bang
	const #49 = Asciz	java/io/IOException;
	const #50 = class	#49;	//  java/io/IOException
	const #51 = Asciz	f;
	const #52 = Asciz	Ljava/io/File;;
	const #53 = Asciz	d;
	const #54 = Asciz	world;
	const #55 = String	#54;	//  world
	const #56 = Asciz	e;
	const #57 = Asciz	java/lang/String;
	const #58 = class	#57;	//  java/lang/String
	const #59 = Method	#58.#8;	//  java/lang/String."<init>":()V
	const #60 = Asciz	other;
	const #61 = String	#60;	//  other
	const #62 = Asciz	SourceFile;
	const #63 = Asciz	Investigation.java;

	{
	public Investigation();
	  Code:
	   Stack=1, Locals=1, Args_size=1
	   0:	aload_0
	   1:	invokespecial	#9; //Method java/lang/Object."<init>":()V
	   4:	return
	  LineNumberTable: 
	   line 5: 0
	  LocalVariableTable: 
	   Start  Length  Slot  Name   Signature
	   0      5      0    this       LInvestigation;

	public static void main(java.lang.String[]);
	  org.aspectj.weaver.MethodDeclarationLineNumber: length = 0x8
	   00 00 00 06 00 00 00 FFFFFF88 
	  Code:
	   Stack=0, Locals=1, Args_size=1
	   0:	return
	  LineNumberTable: 
	   line 8: 0
	  LocalVariableTable: 
	   Start  Length  Slot  Name   Signature
	   0      1      0    args       [Ljava/lang/String;

	public void a();
	  org.aspectj.weaver.MethodDeclarationLineNumber: length = 0x8
	   00 00 00 0C 00 00 00 FFFFFFD9 
	  Code:
	   Stack=2, Locals=1, Args_size=1
	   0:	aload_0
	   1:	dup
	   2:	monitorenter
	   3:	monitorexit
	   4:	return
	  LineNumberTable: 
	   line 13: 0
	   line 16: 4
	  LocalVariableTable: 
	   Start  Length  Slot  Name   Signature
	   0      5      0    this       LInvestigation;

	public void b();
	  org.aspectj.weaver.MethodDeclarationLineNumber: length = 0x8
	   00 00 00 13 00 00 01 38 
	  Code:
	   Stack=2, Locals=2, Args_size=1
	   0:	aload_0
	   1:	dup
	   2:	astore_1
	   3:	monitorenter
	   4:	getstatic	#26; //Field java/lang/System.out:Ljava/io/PrintStream;
	   7:	ldc	#28; //String hello
	   9:	invokevirtual	#34; //Method java/io/PrintStream.println:(Ljava/lang/String;)V
	   12:	aload_1
	   13:	monitorexit
	   14:	goto	20
	   17:	aload_1
	   18:	monitorexit
	   19:	athrow
	   20:	return
	  Exception table:
	   from   to  target type
	     4    14    17   any
	    17    19    17   any
	  LineNumberTable: 
	   line 20: 0
	   line 21: 4
	   line 20: 12
	   line 23: 20
	  LocalVariableTable: 
	   Start  Length  Slot  Name   Signature
	   0      21      0    this       LInvestigation;

	public void c();
	  org.aspectj.weaver.MethodDeclarationLineNumber: length = 0x8
	   00 00 00 1A 00 00 01 FFFFFFB7 
	  Code:
	   Stack=3, Locals=3, Args_size=1
	   0:	aload_0
	   1:	dup
	   2:	astore_1
	   3:	monitorenter
	   4:	new	#37; //class java/io/File
	   7:	dup
	   8:	ldc	#39; //String fred
	   10:	invokespecial	#41; //Method java/io/File."<init>":(Ljava/lang/String;)V
	   13:	astore_2
	   14:	new	#43; //class java/io/FileInputStream
	   17:	dup
	   18:	aload_2
	   19:	invokespecial	#46; //Method java/io/FileInputStream."<init>":(Ljava/io/File;)V
	   22:	pop
	   23:	goto	35
	   26:	pop
	   27:	getstatic	#26; //Field java/lang/System.out:Ljava/io/PrintStream;
	   30:	ldc	#48; //String bang
	   32:	invokevirtual	#34; //Method java/io/PrintStream.println:(Ljava/lang/String;)V
	   35:	aload_1
	   36:	monitorexit
	   37:	goto	43
	   40:	aload_1
	   41:	monitorexit
	   42:	athrow
	   43:	return
	  Exception table:
	   from   to  target type
	     4    26    26   Class java/io/IOException

	     4    37    40   any
	    40    42    40   any
	  LineNumberTable: 
	   line 27: 0
	   line 29: 4
	   line 30: 14
	   line 31: 26
	   line 32: 27
	   line 27: 35
	   line 35: 43
	  LocalVariableTable: 
	   Start  Length  Slot  Name   Signature
	   0      44      0    this       LInvestigation;
	   14      12      2    f       Ljava/io/File;

	public void d();
	  org.aspectj.weaver.MethodDeclarationLineNumber: length = 0x8
	   00 00 00 26 00 00 02 FFFFFFC2 
	  Code:
	   Stack=2, Locals=2, Args_size=1
	   0:	aload_0
	   1:	dup
	   2:	astore_1
	   3:	monitorenter
	   4:	getstatic	#26; //Field java/lang/System.out:Ljava/io/PrintStream;
	   7:	ldc	#28; //String hello
	   9:	invokevirtual	#34; //Method java/io/PrintStream.println:(Ljava/lang/String;)V
	   12:	aload_1
	   13:	monitorexit
	   14:	goto	20
	   17:	aload_1
	   18:	monitorexit
	   19:	athrow
	   20:	aload_0
	   21:	dup
	   22:	astore_1
	   23:	monitorenter
	   24:	getstatic	#26; //Field java/lang/System.out:Ljava/io/PrintStream;
	   27:	ldc	#55; //String world
	   29:	invokevirtual	#34; //Method java/io/PrintStream.println:(Ljava/lang/String;)V
	   32:	aload_1
	   33:	monitorexit
	   34:	goto	40
	   37:	aload_1
	   38:	monitorexit
	   39:	athrow
	   40:	return
	  Exception table:
	   from   to  target type
	     4    14    17   any
	    17    19    17   any
	    24    34    37   any
	    37    39    37   any
	  LineNumberTable: 
	   line 39: 0
	   line 40: 4
	   line 39: 12
	   line 42: 20
	   line 43: 24
	   line 42: 32
	   line 45: 40
	  LocalVariableTable: 
	   Start  Length  Slot  Name   Signature
	   0      41      0    this       LInvestigation;

	public void e();
	  org.aspectj.weaver.MethodDeclarationLineNumber: length = 0x8
	   00 00 00 30 00 00 03 FFFFFF88 
	  Code:
	   Stack=2, Locals=3, Args_size=1
	   0:	aload_0
	   1:	dup
	   2:	astore_1
	   3:	monitorenter
	   4:	getstatic	#26; //Field java/lang/System.out:Ljava/io/PrintStream;
	   7:	ldc	#28; //String hello
	   9:	invokevirtual	#34; //Method java/io/PrintStream.println:(Ljava/lang/String;)V
	   12:	new	#58; //class java/lang/String
	   15:	dup
	   16:	invokespecial	#59; //Method java/lang/String."<init>":()V
	   19:	dup
	   20:	astore_2
	   21:	monitorenter
	   22:	getstatic	#26; //Field java/lang/System.out:Ljava/io/PrintStream;
	   25:	ldc	#61; //String other
	   27:	invokevirtual	#34; //Method java/io/PrintStream.println:(Ljava/lang/String;)V
	   30:	aload_2
	   31:	monitorexit
	   32:	goto	38
	   35:	aload_2
	   36:	monitorexit
	   37:	athrow
	   38:	aload_1
	   39:	monitorexit
	   40:	goto	46
	   43:	aload_1
	   44:	monitorexit
	   45:	athrow
	   46:	return
	  Exception table:
	   from   to  target type
	    22    32    35   any
	    35    37    35   any
	     4    40    43   any
	    43    45    43   any
	  LineNumberTable: 
	   line 49: 0
	   line 50: 4
	   line 51: 12
	   line 52: 22
	   line 51: 30
	   line 49: 38
	   line 55: 46
	  LocalVariableTable: 
	   Start  Length  Slot  Name   Signature
	   0      47      0    this       LInvestigation;

	}
*/

}