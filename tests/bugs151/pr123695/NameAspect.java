
public aspect NameAspect {
    
	
	declare parents: @InjectName * implements Named;
	
	/* 
	 * The injection of that method interferes with the declare
	 * statements in MarkMyMethodsAspect
	 */
	public  String Named.getName()  { return name; }
	private String Named.name;
    
    after(Named newinstance) : execution(Named+.new(..)) && target(newinstance) {
    	System.out.println("A new name was created");
    	newinstance.name = "TikaTikaSlimShady";
    }
}
