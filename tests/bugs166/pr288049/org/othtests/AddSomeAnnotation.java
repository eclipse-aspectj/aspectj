package org.othtests;

public aspect AddSomeAnnotation {

	declare @method : public String MyClass+.do*(..) : @Deprecated;
	
}
