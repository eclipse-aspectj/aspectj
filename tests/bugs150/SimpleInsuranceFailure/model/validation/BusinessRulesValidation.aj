package insurance.model.validation;

import java.util.*;
import insurance.model.*;

public aspect BusinessRulesValidation {

	private Map validatorsByType = new HashMap();

	// RequiresValidation interface

	public interface RequiresValidation {}

	public List RequiresValidation.getValidationErrors() {
	    if (this.validationErrors == null) {
	    	this.validationErrors = new ArrayList();
	    }
	    return validationErrors;
	}

	private List RequiresValidation.validationErrors;

	  // Triggering validation
	void foo(RequiresValidation domainObject) {
	    	throw new SIValidationException(
	    			domainObject, domainObject.getValidationErrors());
	}
	
}
