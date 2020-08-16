/* *******************************************************************
 * Copyright (c) 1999-2001 Xerox Corporation,
 *               2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Xerox/PARC     initial implementation
 *     Wes Isberg     2004 updates
 * ******************************************************************/

package org.aspectj.testing.harness.bridge;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.IMessageHandler;
import org.aspectj.bridge.IMessageHolder;
import org.aspectj.bridge.ISourceLocation;
import org.aspectj.bridge.Message;
import org.aspectj.bridge.MessageHandler;
import org.aspectj.bridge.MessageUtil;
import org.aspectj.testing.run.IRunIterator;
import org.aspectj.testing.util.BridgeUtil;
import org.aspectj.testing.util.options.Option;
import org.aspectj.testing.util.options.Option.InvalidInputException;
import org.aspectj.testing.util.options.Options;
import org.aspectj.testing.util.options.Values;
import org.aspectj.testing.xml.IXmlWritable;
import org.aspectj.testing.xml.SoftMessage;
import org.aspectj.testing.xml.XMLWriter;
import org.aspectj.util.LangUtil;

/**
 * Base class for initialization of components expecting messages, options, files/paths, and source locations (resolved files), and
 * potentially containing child Spec.
 * <p>
 * <u>initialization</u>: This defines bean/xml setters for all. This converts String to IMessage using
 * {@link MessageUtil#readMessage(String)} and String to ISourceLocation using {@link BridgeUtil#makeSourceLocation(input)}. See
 * those APIs for input form and limitations. A Spec also accepts (or rejects) runtime configuration from a parent in {@link
 * adoptParentValues(RT, IMessageHandler)}. Since some children Spec may balk but this parent Spec continue, use {@link
 * getChildren()} to get the full list of children Spec, but {@link getWorkingChildren()} to get the list of children that are not
 * being skipped in accordance with runtime configuration.
 * <p>
 * <u>subclassing</u>: subclasses wishing other than the default behavior for reading String input should override the corresponding
 * (bean) setter. They can also override the add{foo} methods to get notice or modify objects constructed from the input.
 * <p>
 * <u>bean properties</u>: because this is designed to work by standard Java bean introspection, take care to follow bean rules when
 * adding methods. In particular, a property is illegal if the setter takes a different type than the getter returns. That means,
 * e.g., that all List and array[] getters should be named "get{property}[List|Array]". Otherwise the XML readers will silently fail
 * to set the property (perhaps with trace information that the property had no write method or was read-only).
 * <p>
 * <u>Coordination with writers</u>: because this reads the contents of values written by IXmlWritable, they should ensure their
 * values are readable. When flattening and unflattening lists, the convention is to use the {un}flattenList(..) methods in
 * XMLWriter.
 *
 * @see XMLWriter@unflattenList(String)
 * @see XMLWriter@flattenList(List)
 */
abstract public class AbstractRunSpec implements IRunSpec {

	/** true if we expect to use a staging directory */
	boolean isStaging;

	/** true if this spec permits bad input (e.g., to test error handling) */
	boolean badInput;

	protected String description;

	/** optional source location of the specification itself */
	protected ISourceLocation sourceLocation;

	private BitSet skipSet;
	private boolean skipAll;

	protected String xmlElementName; // nonfinal only for clone()
	protected final List<String> keywords;
	protected final IMessageHolder /* IMessage */messages;
	protected final ArrayList<String> options;
	protected final ArrayList<String> paths;
	// XXXXXunused protected final ArrayList /*ISourceLocation*/ sourceLocations; // XXX remove?
	protected final ArrayList<IRunSpec> children;
	protected final ArrayList<DirChanges.Spec> dirChanges;
	protected XMLNames xmlNames;
	protected String comment;

	/** These options are 1:1 with spec, but set at runtime (not saved) */
	public final RT runtime;

	/** if true, then any child skip causes this to skip */
	protected boolean skipIfAnyChildSkipped; // nonfinal only for cloning

	public AbstractRunSpec(String xmlElementName) {
		this(xmlElementName, true);
	}

	public AbstractRunSpec(String xmlElementName, boolean skipIfAnyChildSkipped) {
		if (null == xmlElementName) {
			xmlElementName = "spec";
		}
		this.xmlElementName = xmlElementName;
		messages = new MessageHandler(true);
		options = new ArrayList<>();
		paths = new ArrayList<>();
		// XXXXXunused sourceLocations = new ArrayList();
		keywords = new ArrayList<>();
		children = new ArrayList<>();
		dirChanges = new ArrayList<>();
		xmlNames = XMLNames.DEFAULT;
		runtime = new RT();
		this.skipIfAnyChildSkipped = skipIfAnyChildSkipped;
	}

	/** @param comment ignored if null */
	public void setComment(String comment) {
		if (!LangUtil.isEmpty(comment)) {
			this.comment = comment;
		}
	}

	public void setStaging(boolean staging) {
		isStaging = staging;
	}

	public void setBadInput(boolean badInput) {
		this.badInput = badInput;
	}

	boolean isStaging() {
		return isStaging;
	}

	// ------- description (title, label...)
	public void setDescription(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}

	// ------- source location of the spec

	public void setSourceLocation(ISourceLocation sourceLocation) {
		this.sourceLocation = sourceLocation;
	}

	public ISourceLocation getSourceLocation() {
		return sourceLocation;
	}

	// ------- keywords
	/** @param keyword added after trimming if not empty */
	public void setKeyword(String keyword) {
		addKeyword(keyword);
	}

	/** @return ((null == s) || (0 == s.trim().length())); */
	public static boolean isEmptyTrimmed(String s) {
		return ((null == s) || (0 == s.length()) || (0 == s.trim().length()));
	}

	/** Add keyword if non-empty and not duplicate */
	public void addKeyword(String keyword) {
		if (!isEmptyTrimmed(keyword)) {
			keyword = keyword.trim();
			if (!keywords.contains(keyword)) {
				keywords.add(keyword);
			}
		}
	}

	public void setKeywords(String items) {
		addKeywords(items);
	}

	public void addKeywords(String items) {
		if (null != items) {
			addKeywords(XMLWriter.unflattenList(items));
		}
	}

	public void addKeywords(String[] ra) {
		if (null != ra) {
			for (String element : ra) {
				addKeyword(element);
			}
		}
	}

	public List<String> getKeywordsList() {
		return makeList(keywords);
	}

	// ------- options - String args

	/** @return ArrayList of String options */
	public ArrayList<String> getOptionsList() {
		return makeList(options);
	}

	/** @return String[] of options */
	public String[] getOptionsArray() {
		return options.toArray(new String[0]);
	}

	public void setOption(String option) {
		addOption(option);
	}

	public void addOption(String option) {
		if ((null != option) && (0 < option.length())) {
			options.add(option);
		}
	}

	/** add options (from XML/bean) - removes any existing options */
	public void setOptions(String items) {
		this.options.clear();
		addOptions(items);
	}

	/**
	 * Set options, removing any existing options.
	 *
	 * @param options String[] options to use - may be null or empty
	 */
	public void setOptionsArray(String[] options) {
		this.options.clear();
		if (!LangUtil.isEmpty(options)) {
			this.options.addAll(Arrays.asList(options));
		}
	}

	public void addOptions(String items) {
		if (null != items) {
			addOptions(XMLWriter.unflattenList(items));
		}
	}

	public void addOptions(String[] ra) {
		if (null != ra) {
			for (String element : ra) {
				addOption(element);
			}
		}
	}

	// --------------- (String) paths
	/** @return ArrayList of String paths */
	public List<String> getPathsList() {
		return makeList(paths);
	}

	/** @return String[] of paths */
	public String[] getPathsArray() {
		return paths.toArray(new String[0]);
	}

	public void setPath(String path) {
		addPath(path);
	}

	public void setPaths(String paths) {
		addPaths(paths);
	}

	public void addPath(String path) {
		if (null != path) {
			paths.add(path);
		}
	}

	public void addPaths(String items) {
		if (null != items) {
			addPaths(XMLWriter.unflattenList(items));
		}
	}

	public void addPaths(String[] ra) {
		if (null != ra) {
			for (String element : ra) {
				addPath(element);
			}
		}
	}

	// --------------------- dir changes
	public void addDirChanges(DirChanges.Spec dirChangesSpec) {
		if (null != dirChangesSpec) {
			dirChanges.add(dirChangesSpec);
		}
	}

	// --------------------- messages
	public void setMessage(String message) {
		addMessage(message);
	}

	public void addMessage(IMessage message) {
		if (null != message) {
			if (!messages.handleMessage(message)) {
				String s = "invalid message: " + message;
				throw new IllegalArgumentException(s);
			}
		}
	}

	public void addMessage(String message) {
		if (null != message) {
			addMessage(BridgeUtil.readMessage(message));
		}
	}

	/**
	 * this can ONLY work if each item has no internal comma
	 */
	public void addMessages(String items) {
		if (null != items) {
			String[] ra = XMLWriter.unflattenList(items);
			for (String element : ra) {
				addMessage(element);
			}
		}
	}

	public void addMessages(List messages) {
		if (null != messages) {
			for (Object o : messages) {
				if (o instanceof IMessage) {
					addMessage((IMessage) o);
				} else {
					String m = "not message: " + o;
					addMessage(new Message(m, IMessage.WARNING, null, null));
				}
			}
		}
	}

	/** @return int number of message of this kind (optionally or greater */
	public int numMessages(IMessage.Kind kind, boolean orGreater) {
		return messages.numMessages(kind, orGreater);
	}

	public IMessageHolder getMessages() {
		return messages;
	}

	public void addChild(IRunSpec child) {
		// fyi, child is added when complete (depth-first), not when initialized,
		// so cannot affect initialization of child here
		if (null != child) {
			children.add(child);
		}
	}

	/** @return copy of children list */
	public ArrayList<IRunSpec> getChildren() {
		return makeList(children);
	}

	/** @return copy of children list without children to skip */
	public ArrayList<IRunSpec> getWorkingChildren() {
		if (skipAll) {
			return new ArrayList<>();
		}
		if (null == skipSet) {
			return getChildren();
		}
		ArrayList<IRunSpec> result = new ArrayList<>();
		int i = 0;
		for (Iterator<IRunSpec> iter = children.listIterator(); iter.hasNext(); i++) {
			IRunSpec child = iter.next();
			if (!skipSet.get(i)) {
				result.add(child);
			}
		}
		return result;
	}

	/**
	 * Recursively absorb parent values if different. This implementation calls doAdoptParentValues(..) and then calls this for any
	 * children. This is when skipped children are determined. Children may elect to balk at this point, reducing the number of
	 * children or causing this spec to skip if skipIfAnyChildrenSkipped. For each test skipped, either this doAdoptParentValues(..)
	 * or the child's adoptParentValues(..) should add one info message with the reason this is being skipped. The only reason to
	 * override this would be to NOT invoke the same for children, or to do something similar for children which are not
	 * AbstractRunSpec.
	 *
	 * @param parentRuntime the RT values to adopt - ignored if null
	 * @param handler the IMessageHandler for info messages when skipping
	 * @return false if this wants to be skipped, true otherwise
	 */
	public boolean adoptParentValues(RT parentRuntime, IMessageHandler handler) {
		boolean skipped = false;
		skipAll = false;
		skipSet = new BitSet();
		if (null != parentRuntime) {
			skipped = !doAdoptParentValues(parentRuntime, handler);
			if (skipped && skipIfAnyChildSkipped) { // no need to continue checking
				skipAll = true;
				return false;
			}
			int i = 0;
			for (ListIterator iter = children.listIterator(); iter.hasNext(); i++) {
				IRunSpec child = (IRunSpec) iter.next();
				if (child instanceof AbstractRunSpec) {
					AbstractRunSpec arsChild = (AbstractRunSpec) child;
					if (!arsChild.adoptParentValues(runtime, handler)) {
						skipSet.set(i);
						if (!skipped) {
							skipped = true;
							if (skipIfAnyChildSkipped) { // no need to continue checking
								skipAll = true;
								return false;
							}
						}
					}
				}
			}
		}
		return true;
	}

	/**
	 * Adopt parent values. This implementation makes a local copy. If we interpret (and absorb) any options, they should be removed
	 * from parentRuntime. This sets verbose if different (override) and directly adopts parentOptions if ours is null and otherwise
	 * adds any non-null options we don't already have. setting verbose and adding to parent options. Implementors override this to
	 * affect how parent values are adopted. Implementors should not recurse into children. This method may be called multiple
	 * times, so implementors should not destroy any spec information. Always add an info message when returning false to skip
	 *
	 * @param parentRuntime the RT values to adopt - never null
	 * @return false if this wants to be skipped, true otherwise
	 */
	protected boolean doAdoptParentValues(RT parentRuntime, IMessageHandler handler) {
		if (runtime.verbose != parentRuntime.verbose) {
			runtime.verbose = parentRuntime.verbose;
		}
		if (!LangUtil.isEmpty(runtime.parentOptions)) {
			runtime.parentOptions.clear();
		}
		if (!LangUtil.isEmpty(parentRuntime.parentOptions)) {
			runtime.parentOptions.addAll(parentRuntime.parentOptions);
		}
		return true;
	}

	/**
	 * Implementations call this when signalling skips to ensure consistency in message formatting
	 *
	 * @param handler the IMessageHandler sink - not null
	 * @param reason the String reason to skip - not null
	 */
	protected void skipMessage(IMessageHandler handler, String reason) {
		LangUtil.throwIaxIfNull(handler, "handler");
		LangUtil.throwIaxIfNull(handler, "reason");
		// XXX for Runs, label does not identify the test
		String label = toString();
		MessageUtil.info(handler, "skipping \"" + label + "\" because " + reason);
	}

	// --------------------------- writing xml - would prefer castor..

	/**
	 * Control XML output by renaming or suppressing output for attributes and subelements. Subelements are skipped by setting the
	 * XMLNames booleans to false. Attributes are skipped by setting their name to null.
	 *
	 * @param names XMLNames with new names and/or suppress flags.
	 */
	protected void setXMLNames(XMLNames names) {
		if (null != names) {
			xmlNames = names;
		}
	}

	// /** @return null if value is null or name="{value}" otherwise */
	// private String makeAttr(XMLWriter out, String name, String value) {
	// if (null == value) {
	// return null;
	// }
	// return XMLWriter.makeAttribute(name, value);
	// }
	//
	// /** @return null if list is null or empty or name="{flattenedList}" otherwise */
	// private String makeAttr(XMLWriter out, String name, List list) {
	// if (LangUtil.isEmpty(list)) {
	// return null;
	// }
	// String flat = XMLWriter.flattenList(list);
	// return XMLWriter.makeAttribute(name, flat);
	// }
	//
	/** @return true if writeAttributes(..) will produce any output */
	protected boolean haveAttributes() {
		return ((!LangUtil.isEmpty(xmlNames.descriptionName) && !LangUtil.isEmpty(description))
				|| (!LangUtil.isEmpty(xmlNames.keywordsName) && !LangUtil.isEmpty(keywords))
				|| (!LangUtil.isEmpty(xmlNames.optionsName) && !LangUtil.isEmpty(options)) || (!LangUtil
						.isEmpty(xmlNames.pathsName) && !LangUtil.isEmpty(paths)));
	}

	/**
	 * Write attributes without opening or closing elements/attributes. An attribute is written only if the value is not empty and
	 * the name in xmlNames is not empty
	 */
	protected void writeAttributes(XMLWriter out) {
		if (!LangUtil.isEmpty(xmlNames.descriptionName) && !LangUtil.isEmpty(description)) {
			out.printAttribute(xmlNames.descriptionName, description);
		}
		if (!LangUtil.isEmpty(xmlNames.keywordsName) && !LangUtil.isEmpty(keywords)) {
			out.printAttribute(xmlNames.keywordsName, XMLWriter.flattenList(keywords));
		}
		if (!LangUtil.isEmpty(xmlNames.optionsName) && !LangUtil.isEmpty(options)) {
			out.printAttribute(xmlNames.optionsName, XMLWriter.flattenList(options));
		}
		if (!LangUtil.isEmpty(xmlNames.pathsName) && !LangUtil.isEmpty(paths)) {
			out.printAttribute(xmlNames.pathsName, XMLWriter.flattenList(paths));
		}
		if (!LangUtil.isEmpty(xmlNames.commentName) && !LangUtil.isEmpty(comment)) {
			out.printAttribute(xmlNames.commentName, comment);
		}
		if (isStaging && !LangUtil.isEmpty(xmlNames.stagingName)) {
			out.printAttribute(xmlNames.stagingName, "true");
		}
		if (badInput && !LangUtil.isEmpty(xmlNames.badInputName)) {
			out.printAttribute(xmlNames.badInputName, "true");
		}
	}

	/**
	 * The default implementation writes everything as attributes, then subelements for dirChanges, messages, then subelements for
	 * children. Subclasses that override may delegate back for any of these. Subclasses may also set XMLNames to name or suppress
	 * any attribute or subelement.
	 *
	 * @see writeMessages(XMLWriter)
	 * @see writeChildren(XMLWriter)
	 * @see IXmlWritable#writeXml(XMLWriter)
	 */
	@Override
	public void writeXml(XMLWriter out) {
		out.startElement(xmlElementName, false);
		writeAttributes(out);
		out.endAttributes();
		if (!xmlNames.skipMessages) {
			writeMessages(out);
		}
		if (!xmlNames.skipChildren) {
			writeChildren(out);
		}
		out.endElement(xmlElementName);
	}

	/**
	 * Write messages. Assumes attributes are closed, can write child elements of current element.
	 */
	protected void writeMessages(XMLWriter out) {
		if (0 < messages.numMessages(null, true)) {
			SoftMessage.writeXml(out, messages);
		}

	}

	/**
	 * Write children. Assumes attributes are closed, can write child elements of current element.
	 */
	protected void writeChildren(XMLWriter out) {
		if (0 < children.size()) {
			for (IRunSpec iRunSpec : children) {
				IXmlWritable self = iRunSpec;
				self.writeXml(out);
			}
		}
	}

	// --------------------------- logging

	public void printAll(PrintStream out, String prefix) {
		out.println(prefix + toString());
		for (IRunSpec iRunSpec : children) {
			AbstractRunSpec child = (AbstractRunSpec) iRunSpec; // IRunSpec
			child.printAll(out, prefix + "    ");
		}
	}

	/**
	 * default implementation returns the description if not empty or the unqualified class name otherwise. Subclasses should not
	 * call toString from here unless they reimplement it.
	 *
	 * @return name of this thing or type
	 */
	protected String getPrintName() {
		if (!LangUtil.isEmpty(description)) {
			return description;
		} else {
			return LangUtil.unqualifiedClassName(this);
		}
	}

	/** @return summary count of spec elements */
	@Override
	public String toString() {
		return getPrintName() + "(" + containedSummary() + ")";
	}

	/** @return String of the form (# [options|paths|locations|messages]).. */
	protected String containedSummary() {
		StringBuffer result = new StringBuffer();
		addListCount("options", options, result);
		addListCount("paths", paths, result);
		// XXXXXunused addListCount("sourceLocations", sourceLocations, result);
		List<IMessage> messagesList = messages.getUnmodifiableListView();
		addListCount("messages", messagesList, result);

		return result.toString().trim();
	}

	public String toLongString() {
		String mssg = "";
		if (0 < messages.numMessages(null, true)) {
			mssg = " expected messages (" + MessageUtil.renderCounts(messages) + ")";
		}
		return getPrintName() + containedToLongString() + mssg.trim();
	}

	/** @return String of the form (# [options|paths|locations|messages]).. */
	protected String containedToLongString() {
		StringBuffer result = new StringBuffer();
		addListEntries("options", options, result);
		addListEntries("paths", paths, result);
		// XXXXXunused addListEntries("sourceLocations", sourceLocations, result);
		List<IMessage> messagesList = messages.getUnmodifiableListView();
		addListEntries("messages", messagesList, result);

		return result.toString();
	}

	protected void initClone(AbstractRunSpec spec) throws CloneNotSupportedException {
		/*
		 * clone associated objects only if not (used as?) read-only.
		 */
		spec.badInput = badInput;
		spec.children.clear();
		for (IRunSpec child : children) {
			// require all child classes to support clone?
			if (child instanceof AbstractRunSpec) {
				spec.addChild((AbstractRunSpec) ((AbstractRunSpec) child).clone());
			} else {
				throw new Error("unable to clone " + child);
			}
		}
		spec.comment = comment;
		spec.description = description;
		spec.dirChanges.clear();
		spec.dirChanges.addAll(dirChanges);
		spec.isStaging = spec.isStaging;
		spec.keywords.clear();
		spec.keywords.addAll(keywords);
		spec.messages.clearMessages();
		MessageUtil.handleAll(spec.messages, messages, false);
		spec.options.clear();
		spec.options.addAll(options);
		spec.paths.clear();
		spec.paths.addAll(paths);
		spec.runtime.copy(runtime);
		spec.skipAll = skipAll;
		spec.skipIfAnyChildSkipped = skipIfAnyChildSkipped;
		if (null != skipSet) {
			spec.skipSet = new BitSet();
			spec.skipSet.or(skipSet);
		}
		// spec.sourceLocation = sourceLocation;
		// spec.sourceLocations.clear();
		// XXXXXunused spec.sourceLocations.addAll(sourceLocations);
		spec.xmlElementName = xmlElementName;
		spec.xmlNames = ((AbstractRunSpec.XMLNames) xmlNames.clone());
	}

	private static void addListCount(String name, List<?> list, StringBuffer sink) {
		int size = list.size();
		if ((null != list) && (0 < size)) {
			sink.append(" " + size + " ");
			sink.append(name);
		}
	}

	private static void addListEntries(String name, List<?> list, StringBuffer sink) {
		if ((null != list) && (0 < list.size())) {
			sink.append(" " + list.size() + " ");
			sink.append(name);
			sink.append(": ");
			sink.append(list.toString());
		}
	}

	private <T> ArrayList<T> makeList(List<T> list) {
		ArrayList<T> result = new ArrayList<>();
		if (null != list) {
			result.addAll(list);
		}
		return result;
	}

	/**
	 * Subclasses use this to rename attributes or omit attributes or subelements. To suppress output of an attribute, pass "" as
	 * the name of the attribute. To use default entries, pass null for that entry. XXX this really should be replaced with nested
	 * properties associated logical name with actual name (or placeholders for "unused" and "default").
	 */
	public static class XMLNames {
		public static final XMLNames DEFAULT = new XMLNames(null, "description", "sourceLocation", "keywords", "options", "paths",
				"comment", "staging", "badInput", false, false, false);
		final String descriptionName;
		final String sourceLocationName;
		final String keywordsName;
		final String optionsName;
		final String pathsName;
		final String commentName;
		final String stagingName;
		final String badInputName;
		final boolean skipDirChanges;
		final boolean skipMessages;
		final boolean skipChildren;

		@Override
		protected Object clone() {
			return new XMLNames(null, descriptionName, sourceLocationName, keywordsName, optionsName, pathsName, commentName,
					stagingName, badInputName, skipDirChanges, skipMessages, skipChildren);
		}

		// not runtime, skipAll, skipIfAnyChildSkipped, skipSet
		// sourceLocations
		/**
		 * reset all names/behavior or pass defaultNames as the defaults for any null elements
		 */
		XMLNames(XMLNames defaultNames, String descriptionName, String sourceLocationName, String keywordsName, String optionsName,
				String pathsName, String commentName, String stagingName, String badInputName, boolean skipDirChanges,
				boolean skipMessages, boolean skipChildren) {
			this.skipDirChanges = skipDirChanges;
			this.skipMessages = skipMessages;
			this.skipChildren = skipChildren;
			if (null != defaultNames) {
				this.descriptionName = (null != descriptionName ? descriptionName : defaultNames.descriptionName);
				this.sourceLocationName = (null != sourceLocationName ? sourceLocationName : defaultNames.sourceLocationName);
				this.keywordsName = (null != keywordsName ? keywordsName : defaultNames.keywordsName);
				this.optionsName = (null != optionsName ? optionsName : defaultNames.optionsName);
				this.pathsName = (null != pathsName ? pathsName : defaultNames.pathsName);
				this.commentName = (null != commentName ? commentName : defaultNames.commentName);
				this.stagingName = (null != stagingName ? stagingName : defaultNames.stagingName);
				this.badInputName = (null != badInputName ? badInputName : defaultNames.badInputName);
			} else {
				this.descriptionName = descriptionName;
				this.sourceLocationName = sourceLocationName;
				this.keywordsName = keywordsName;
				this.optionsName = optionsName;
				this.pathsName = pathsName;
				this.commentName = commentName;
				this.stagingName = stagingName;
				this.badInputName = badInputName;
			}
		}
	}

	/** subclasses implement this to create and set up a run */
	@Override
	abstract public IRunIterator makeRunIterator(Sandbox sandbox, Validator validator);

	/** segregate runtime-only state in spec */
	public static class RT {
		/** true if we should emit verbose messages */
		private boolean verbose;

		/** null unless parent set options for children to consider */
		final private ArrayList<String> parentOptions;

		public RT() {
			parentOptions = new ArrayList<>();
		}

		public boolean isVerbose() {
			return verbose;
		}

		/**
		 * Set parent options - old options destroyed. Will result in duplicates if duplicates added. Null or empty entries are
		 * ignored
		 *
		 * @param options ignored if null or empty
		 */
		public void setOptions(String[] options) {
			parentOptions.clear();
			if (!LangUtil.isEmpty(options)) {
				for (String option : options) {
					if (!LangUtil.isEmpty(option)) {
						parentOptions.add(option);
					}
				}
			}
		}

		/**
		 * Copy values from another RT
		 *
		 * @param toCopy the RT to copy from
		 * @throws IllegalArgumentException if toCopy is null
		 */
		public void copy(RT toCopy) {
			LangUtil.throwIaxIfNull(toCopy, "parent");
			parentOptions.clear();
			parentOptions.addAll(toCopy.parentOptions);
			verbose = toCopy.verbose;
		}

		/**
		 * Return any parent option accepted by validOptions, optionally removing the parent option.
		 *
		 * @param validOptions String[] of options to extract
		 * @param remove if true, then remove any parent option matched
		 * @return String[] containing any validOptions[i] in parentOptions
		 *
		 */
		public Values extractOptions(Options validOptions, boolean remove, StringBuffer errors) {
			Values result = Values.EMPTY;
			if (null == errors) {
				errors = new StringBuffer();
			}
			if (null == validOptions) {
				errors.append("null options");
				return result;
			}
			if (LangUtil.isEmpty(parentOptions)) {
				return result;
			}
			// boolean haveOption = false;
			String[] parents = parentOptions.toArray(new String[0]);
			try {
				result = validOptions.acceptInput(parents);
			} catch (InvalidInputException e) {
				errors.append(e.getFullMessage());
				return result;
			}
			if (remove) {
				Option.Value[] values = result.asArray();
				for (int i = 0; i < values.length; i++) {
					Option.Value value = values[i];
					if (null == value) {
						continue;
					}
					final int max = i + value.option.numArguments();
					if (max > i) {
						if (max >= parents.length) {
							errors.append("expecting more args for " + value.option + " at [" + i + "]: " + Arrays.asList(parents));
							return result;
						}
						// XXX verify
						for (int j = i; j < max; j++) {
							parentOptions.remove(parents[j]);
						}
						i = max - 1;
					}
				}
			}
			return result;
		}

		/**
		 * Return any parent option which has one of validOptions as a prefix, optionally absorbing (removing) the parent option.
		 *
		 * @param validOptions String[] of options to extract
		 * @param absorb if true, then remove any parent option matched
		 * @return String[] containing any validOptions[i] in parentOptions (at most once)
		 */
		public String[] extractOptions(String[] validOptions, boolean absorb) {
			if (LangUtil.isEmpty(validOptions) || LangUtil.isEmpty(parentOptions)) {
				return new String[0];
			}
			ArrayList<String> result = new ArrayList<>();
			// boolean haveOption = false;
			for (String option : validOptions) {
				if (LangUtil.isEmpty(option)) {
					continue;
				}
				for (ListIterator<String> iter = parentOptions.listIterator(); iter.hasNext();) {
					String parentOption = iter.next();
					if (parentOption.startsWith(option)) {
						result.add(parentOption);
						if (absorb) {
							iter.remove();
						}
					}
				}
			}
			return result.toArray(new String[0]);
		}

		/** Get ListIterator that permits removals */
		ListIterator<String> getListIterator() {
			return parentOptions.listIterator();
		}

		/**
		 * Enable verbose logging
		 *
		 * @param verbose if true, do verbose logging
		 */
		public void setVerbose(boolean verbose) {
			if (this.verbose != verbose) {
				this.verbose = verbose;
			}
		}
	} // class RT

}
