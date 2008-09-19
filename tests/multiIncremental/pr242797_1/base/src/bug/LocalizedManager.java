/*
 * Created on Jul 25, 2008
 */
package bug;

import java.util.List;


public interface LocalizedManager extends Manager{
	
	public Class<? extends Localized> getLocalizedType();
	
	public List<? extends Localized> getAllInLocale(String language);
	
	public List<? extends Localized> getAllInLocales(List<String> languages);
	
	public List<? extends Localized> getAllInDefaultLocales();
	
	public List<? extends Localized> getBestInLocales(List<String> languages);
	
	public List<? extends Localized> getBestInDefaultLocales();
	
	public List<? extends Localized> removeWeakLanguageMatches(List<? extends Localized> localized);
	
	public List<? extends Localized> removeWeakLanguageMatches(List<? extends Localized> localized, List<String> languageOrder);
	
	public DuplicateStrategy getLocalizedDuplicateStrategy();
	
	public List<String> getDefaultLanguageOrder();

}
