package pkg;

public aspect ANewTypeWizardPage {

	public static int NewTypeWizardPage.F_PRIVILEGED = 1;
	
	after() : execution(* NewTypeWizardPage.setModifiers(..)) {
		if(NewTypeWizardPage.F_PRIVILEGED != 0) {
			
		}
	}
	
	before() : execution(int NewTypeWizardPage.getModifiers()) {
		int i = 0;
		if (((NewTypeWizardPage)thisJoinPoint.getThis()).isTrue()) {
			i = NewTypeWizardPage.F_PRIVILEGED;
		}
	}
	
}
