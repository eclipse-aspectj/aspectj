/*
 * Created on 18-Jul-2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package insurance.model.validation;

import java.util.List;

/**
 * @author AndyClement
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class SIValidationException extends RuntimeException {

	private List validationErrors;
	
	public SIValidationException(BusinessRulesValidation.RequiresValidation obj,java.util.List validationErrors) {
		super("Business object " + obj + " failed validation");
		this.validationErrors = validationErrors;
	}

	public List getValidationErrors() {
		return validationErrors;
	}

}
