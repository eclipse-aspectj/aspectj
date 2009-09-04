package org.tests.atann;

public aspect AddAnnotations {
	
	declare @method : public int *do*(..) : @Traced;

}
