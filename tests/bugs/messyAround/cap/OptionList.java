package cap; 

import java.lang.reflect.*;
import java.util.*;
import java.net.*;
import java.text.*;

/**
 * This class builds a list of &lt;option&gt; HTML elements given a data object,
 * typically a GAPI output object, and a list of accessor method names.  
 * <p>
 * <b>Usage:</b><pre>
 *   // Create the bank account list select
 *   RBBankAcctList2Input acctIn = new RBBankAcctList2Input();
 *   initApiHeader(acctIn.getHeader(),sessInfo);
 *   ArrayList accts = new ArrayList();
 *   getBankAccounts(acctIn,accts);
 *   
 *   String ol = OptionList.createListHtmlFromApi(accts.toArray(),
 *                                   new String[]{"getBankAcctNbr","getBankRtgNbr","getBankAcctTyp"},
 *                                   new String[]{"getBankAcctNbr"},
 *                                   new MessageFormat("{0}"),
 *                                   Integer.parseInt(acctIndex));
 * 
 * </pre>
 * @author Rich Price
 */
class OptionList
{
	private static final String OPTION_PATTERN = "<option value=\"{0}\" {1}>{2}</option>";
	private static final Object[] GETTER_ARGS = new Object[0];
	private static final Class[] GETTER_ARG_TYPES = new Class[0];
	private static final String DELIM = "&";
    
	/**
	 * Parses the value string and returns a HashMap of name/value pairs
	 * @return A HashMap of name/value pairs. 
	 * @see createListHtmlFromApi
	 */
	public static HashMap getSelectedValues(String optionListValueString)
	{
		HashMap map = new HashMap();
		if ( optionListValueString != null )
		{
			StringTokenizer lex = new StringTokenizer(optionListValueString,DELIM + "=",false);
			while ( lex.hasMoreTokens() )
				map.put(lex.nextToken(),lex.nextToken());
		}
		return map;            
	}
    
	/**
	 * This method creates a String of HTML &lt;option&gt; elements in the following
	 * format:<p>
	 * <pre>
	 * &lt;option value="valueName1=value1^valueName2=value2"&gt; optionValues &lt;option&gt;
	 * </pre>
	 * @param api An array of Objects, typically a GAPI output object, from which data
	 * will be retrieved by name(s).
	 * @param valueNames An array of method names declared in <code>api</code>. Only
	 * public methods taking zero arguments can be used.  Each non-null value
	 * is used to create a value string for the particular HTML option element in the form:
	 * valueName1=value1^valueName2=value2...where valueName[n] is the method name.
	 * For convenience, the getValues() method will return a HashMap of these name/value
	 * pairs.
	 * @param optionNames An array of method names declared in <code>api</code>. Only
	 * public methods taking zero arguments can be listed.  Each non-null value
	 * is used to create a parameter list to pass to a supplied MessageFormat object.
	 * Each value retrieved from the api object will be substituted using the MessageFormat
	 * object, the resulting String is used to create the optionValues string that is
	 * displayed to the user.
	 * @param selectedIndex The index of the option that should be selected. If -1, nothing
	 * will be selected.
	 * 
	 */
	public static String createListHtmlFromApi(Object[] api, 
									String[] valueNames,
									String[] optionNames,
									MessageFormat optionFormat,
									int selectedIndex )
	{
		StringBuffer html = new StringBuffer();
		for ( int apiIndex = 0; apiIndex < api.length; apiIndex++ )
		{
			final String[] messageArgs = new String[3];        
			// for each valueName, use reflection to look up data from the api
			StringBuffer buf = new StringBuffer();
			for ( int i = 0; i < valueNames.length; i++ )
			{
				try
				{
					Method m = api[apiIndex].getClass().getMethod(valueNames[i], GETTER_ARG_TYPES);
					String value = m.invoke(api[apiIndex],GETTER_ARGS).toString();

					if ( value != null && value.length() > 0 )
					{
						if ( buf.length() > 0 )
							buf.append(DELIM);
						buf.append(valueNames[i]).append("=").append(value);
					}    
				}
				catch (Exception e) {}
			}
			// set the first and second value arguments for the pattern
			messageArgs[0] = buf.toString();
			if ( apiIndex == selectedIndex )
				messageArgs[1] = "selected";
			else
				messageArgs[1] = "";
                        
			// now, handle the option part
			buf.setLength(0);
			String[] optionFormatArgs = new String[optionNames.length];
			for ( int i = 0; i < optionNames.length; i++ )
			{
				try
				{
					optionFormatArgs[i] = "";    
					Method m = api[apiIndex].getClass().getMethod(optionNames[i],GETTER_ARG_TYPES);
					String value = m.invoke(api[apiIndex],GETTER_ARGS).toString();
					if ( value != null )
						optionFormatArgs[i] = value;
				}
				catch(Exception e) {}
			}
            
			messageArgs[2] = optionFormat.format(optionFormatArgs,buf,new FieldPosition(0)).toString();
			html.append(MessageFormat.format(OPTION_PATTERN,messageArgs));
		}
		return html.toString();
	}
    
	public static void main(String[] args) throws Exception
	{
		OptionList.createListHtmlFromApi(new Object[]{new String()},new String[]{"getFoo"},new String[]{"getFoo"},new MessageFormat("{0}"),-1);
	}
    
}