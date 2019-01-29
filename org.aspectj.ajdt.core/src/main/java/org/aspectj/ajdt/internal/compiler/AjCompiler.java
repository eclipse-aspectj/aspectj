///* *******************************************************************
// * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
// * All rights reserved. 
// * This program and the accompanying materials are made available 
// * under the terms of the Eclipse Public License v1.0 
// * which accompanies this distribution and is available at 
// * http://www.eclipse.org/legal/epl-v10.html 
// *  
// * Contributors: 
// *     PARC     initial implementation 
// * ******************************************************************/
//
//
//package org.aspectj.ajdt.internal.compiler;
//
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.Collection;
//import java.util.Collections;
//import java.util.Enumeration;
//import java.util.Hashtable;
//import java.util.Iterator;
//import java.util.List;
//import java.util.Map;
//
//import org.aspectj.ajdt.internal.compiler.lookup.EclipseFactory;
//import org.aspectj.ajdt.internal.compiler.lookup.EclipseSourceLocation;
//import org.aspectj.bridge.AbortException;
//import org.aspectj.bridge.IMessage;
//import org.aspectj.bridge.IMessageHandler;
//import org.aspectj.bridge.ISourceLocation;
//import org.aspectj.bridge.IMessage.Kind;
//import org.aspectj.weaver.IClassFileProvider;
//import org.aspectj.weaver.IWeaveRequestor;
//import org.aspectj.weaver.bcel.BcelWeaver;
//import org.aspectj.weaver.bcel.BcelWorld;
//import org.aspectj.weaver.bcel.Pause;
//import org.aspectj.weaver.bcel.UnwovenClassFile;
//import org.aspectj.weaver.bcel.UnwovenClassFileWithThirdPartyManagedBytecode;
//import org.eclipse.jdt.core.compiler.IProblem;
//import org.eclipse.jdt.internal.compiler.ClassFile;
//import org.eclipse.jdt.internal.compiler.CompilationResult;
//import org.eclipse.jdt.internal.compiler.Compiler;
//import org.eclipse.jdt.internal.compiler.ICompilerRequestor;
//import org.eclipse.jdt.internal.compiler.IErrorHandlingPolicy;
//import org.eclipse.jdt.internal.compiler.IProblemFactory;
//import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
//import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
//import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
//import org.eclipse.jdt.internal.compiler.impl.ReferenceContext;
//import org.eclipse.jdt.internal.compiler.problem.DefaultProblem;
//import org.eclipse.jdt.internal.compiler.problem.ProblemSeverities;
//
//
//public class AjCompiler extends Compiler {
//	
//	private List /*InterimResult*/ resultsPendingWeave = new ArrayList();
//	private BcelWorld bcelWorld;
//	private BcelWeaver bcelWeaver;
//	private IIntermediateResultsRequestor intermediateResultRequestor;
//	private IOutputClassFileNameProvider nameProvider;
//	private List /*<InterimResult>*/ binarySources = new ArrayList();
//	private boolean skipTheWeavePhase = false;
//	private boolean reportedErrors = false;
//	private WeaverMessageHandler wmHandler;
//	private boolean isBatchCompile = false;
//	private Collection /*InterimResult*/ resultSetForFullWeave = Collections.EMPTY_LIST;
//	
//	public interface IIntermediateResultsRequestor {
//		void acceptResult(InterimResult intRes);
//	}
//	
//	public interface IOutputClassFileNameProvider {
//		String getOutputClassFileName(char[] eclipseClassFileName, CompilationResult result);
//	}
//	
//	public AjCompiler(
//		INameEnvironment environment,
//		IErrorHandlingPolicy policy,
//		Map settings,
//		ICompilerRequestor requestor,
//		IProblemFactory problemFactory) {
//		super(environment, policy, settings, requestor, problemFactory);
//	}
//
//	public AjCompiler(
//		INameEnvironment environment,
//		IErrorHandlingPolicy policy,
//		Map settings,
//		ICompilerRequestor requestor,
//		IProblemFactory problemFactory,
//		boolean parseLiteralExpressionsAsConstants) {
//		super(
//			environment,
//			policy,
//			settings,
//			requestor,
//			problemFactory,
//			parseLiteralExpressionsAsConstants);
//	}
//	
//		
//	public void setWeaver(BcelWeaver weaver) {
//		this.bcelWeaver = weaver;
//	}
//
//	public void setWorld(BcelWorld world) {
//		this.bcelWorld = world;
//		IMessageHandler msgHandler = world.getMessageHandler();
//		wmHandler = new WeaverMessageHandler(msgHandler);
//		world.setMessageHandler(wmHandler);
//	}
//
//	public void prepareForBatchCompilation() {
//		isBatchCompile = true;
//	}
//	
//	public void setIntermediateResultsRequestor(IIntermediateResultsRequestor intReq) {
//		this.intermediateResultRequestor = intReq;
//	}
//	
//	public void setNoWeave(boolean noWeave) {
//		skipTheWeavePhase = noWeave;
//	}
//	
//	public void setFullWeaveResults(Collection compilationResults) {
//		resultSetForFullWeave = compilationResults;
//	}
//	
//	public void setOutputFileNameProvider(IOutputClassFileNameProvider p) {
//		this.nameProvider = p;
//	}
//	
//	public void addBinarySourceFiles(Map binarySourceEntries) {
//		// Map is fileName |-> List<UnwovenClassFile>
//		for (Iterator binIter = binarySourceEntries.keySet().iterator(); binIter.hasNext();) {
//			String sourceFileName = (String) binIter.next();
//			List unwovenClassFiles = (List) binarySourceEntries.get(sourceFileName);
//			
//			CompilationResult result = new CompilationResult(sourceFileName.toCharArray(),0,0,20);
//			result.noSourceAvailable();
//			InterimResult binarySource = new InterimResult(result,nameProvider);
//			binarySource.unwovenClassFiles = new UnwovenClassFile[unwovenClassFiles.size()];
//			int index = 0;
//			for (Iterator iter = unwovenClassFiles.iterator(); iter.hasNext();) {
//				UnwovenClassFile element = (UnwovenClassFile) iter.next();
//				binarySource.unwovenClassFiles[index] = element;
//				AjClassFile ajcf = new AjClassFile(element.getClassName().replace('.', '/').toCharArray(),
//												   element.getBytes());
//				result.record(ajcf.fileName(),ajcf); 
//				index++;
//			}
//			binarySources.add(binarySource);
//		}
//	}
//	
//	
//	/**
//	 * In addition to processing each compilation unit in the normal ways, 
//	 * we also need to do weaving for inter-type declarations.  This
//	 * must be done before we use the signatures of these types to do any
//	 * name binding.
//	 */
//	public void process(CompilationUnitDeclaration unit, int i) {
//		EclipseFactory world = 
//			EclipseFactory.fromLookupEnvironment(lookupEnvironment);
//		world.showMessage(IMessage.INFO, "compiling " + new String(unit.getFileName()), null, null);
//		super.process(unit, i);
//				
//		world.finishedCompilationUnit(unit);
//	}
//	
//	
//	/* (non-Javadoc)
//	 * @see org.eclipse.jdt.internal.compiler.Compiler#compile(org.eclipse.jdt.internal.compiler.env.ICompilationUnit[])
//	 * We override this method to defer handing of results to the caller until the weave phase has 
//	 * completed too. That way the class files are in final form and all error messages relating to the
//	 * compilation unit have been reported (which is the contract the JDT expects from this method).
//	 */
//	public void compile(ICompilationUnit[] sourceUnits) {
//		try {
//			resultsPendingWeave = new ArrayList();
//			reportedErrors = false;
//			super.compile(sourceUnits);
//			Pause.pause("After super compile");
//			try {
//				if (!(skipTheWeavePhase || reportedErrors))  {
//					weaveResults();
//					Pause.pause("After weave");
//				} else {
//					notifyRequestor();  // weaver notifies as it goes along...
//				}
//			} catch (IOException ex) {
//				// TODO
//				ex.printStackTrace();
//			}
//		} finally {
//			cleanup();
//		}
//	}
//	
//	
//	/* (non-Javadoc)
//	 * @see org.eclipse.jdt.internal.compiler.Compiler#registerCompilationResult(org.eclipse.jdt.internal.compiler.CompilationResult)
//	 */
//	protected void registerCompilationResult(int index, CompilationResult result) {
//		InterimResult intRes = new InterimResult(result,nameProvider);
//		resultsPendingWeave.add(intRes);
//		if (result.hasErrors()) reportedErrors = true;
//		if (intermediateResultRequestor != null) {
//			intermediateResultRequestor.acceptResult(intRes);
//		}
//	}
//	
//	/* (non-Javadoc)
//	 * @see org.eclipse.jdt.internal.compiler.Compiler#reset()
//	 * Super does this too early for us...
//	 */
//	public void reset() {
//		// no-op, super calls this too early for us...
//	}
//	
//	/* (non-Javadoc)
//	 * @see org.eclipse.jdt.internal.compiler.Compiler#resolve(org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration, org.eclipse.jdt.internal.compiler.env.ICompilationUnit, boolean, boolean, boolean)
//	 */
//	public CompilationUnitDeclaration resolve(CompilationUnitDeclaration unit,
//			ICompilationUnit sourceUnit, boolean verifyMethods,
//			boolean analyzeCode, boolean generateCode) {
//		// TODO include weave phase in resolution too...
//		return super.resolve(unit, sourceUnit, verifyMethods, analyzeCode,
//				generateCode);
//	}
//	
//	
//	// AspectJ helper methods, called from compile...
//	private void weaveResults() throws IOException {
//		// ensure weaver state is set up correctly
//		for (Iterator iter = resultsPendingWeave.iterator(); iter.hasNext();) {
//			InterimResult iresult = (InterimResult) iter.next();
//			for (int i = 0; i < iresult.unwovenClassFiles.length; i++) {
//				bcelWeaver.addClassFile(iresult.unwovenClassFiles[i]);
//			}			
//		}
//		Pause.pause("After adding class files to weaver");
//		bcelWeaver.prepareForWeave();
//		Pause.pause("After preparing for weave");
//		if (isBatchCompile) {
//			resultsPendingWeave.addAll(binarySources);  
//			// passed into the compiler, the set of classes in injars and inpath...
//		} else if (bcelWeaver.needToReweaveWorld()) {
//			addAllKnownClassesToWeaveList();
//		}
//		Pause.pause("After adding binary sources to weaver");
//		bcelWeaver.weave(new WeaverAdaptor(this));
//	}
//	
//	private void notifyRequestor() {
//		for (Iterator iter = resultsPendingWeave.iterator(); iter.hasNext();) {
//			InterimResult iresult = (InterimResult) iter.next();
//			requestor.acceptResult(iresult.result.tagAsAccepted());
//		}
//	}
//	
//	private void cleanup() {
//		resultsPendingWeave = null;
//		isBatchCompile = false;
//		binarySources = new ArrayList();
//		resultSetForFullWeave = Collections.EMPTY_LIST;
//		super.reset();
//		Pause.pause("After notify and cleanup");
//	}
//	
//	private void addAllKnownClassesToWeaveList() {
//		// results pending weave already has some results from this (incremental) compile
//		// add in results from any other source
//		for (Iterator iter = resultSetForFullWeave.iterator(); iter.hasNext();) {
//			InterimResult ir = (InterimResult) iter.next();
//			if (!resultsPendingWeave.contains(ir)) {  // equality based on source file name...
//				ir.result.hasBeenAccepted = false;  // it may have been accepted before, start again
//				resultsPendingWeave.add(ir);
//			}			
//		}
//	}
//	
//	// Helper class that feeds input to the weaver, and accepts results
//	// =======================================================================================
//	private static class WeaverAdaptor implements IClassFileProvider, IWeaveRequestor, Iterator {
//		
//		private AjCompiler compiler;
//		private Iterator resultIterator;
//		private int classFileIndex = 0;
//		private InterimResult nowProcessing;
//		private InterimResult lastReturnedResult;
////		private CompilationResult lastAcceptedResult;
//		private boolean finalPhase = false;
//		
//		
//		public WeaverAdaptor(AjCompiler forCompiler) { this.compiler = forCompiler; }
//		
//		/* (non-Javadoc)
//		 * @see org.aspectj.weaver.IClassFileProvider#getClassFileIterator()
//		 */
//		public Iterator getClassFileIterator() {
//			classFileIndex = 0;
//			nowProcessing = null;
//			lastReturnedResult = null;
//			resultIterator = compiler.resultsPendingWeave.iterator();
//			return this;
//		}
//		/* (non-Javadoc)
//		 * @see org.aspectj.weaver.IClassFileProvider#getRequestor()
//		 */
//		public IWeaveRequestor getRequestor() {
//			return this;
//		}
//		/* (non-Javadoc)
//		 * @see org.aspectj.weaver.IWeaveRequestor#acceptResult(org.aspectj.weaver.bcel.UnwovenClassFile)
//		 */
//		public void acceptResult(UnwovenClassFile result) {
//			char[] key = result.getClassName().replace('.','/').toCharArray();
//			removeFromHashtable(lastReturnedResult.result.compiledTypes,key);
//			String className = result.getClassName().replace('.', '/');
//			AjClassFile ajcf = new AjClassFile(className.toCharArray(),
//											   result.getBytes());
//			lastReturnedResult.result.record(ajcf.fileName(),ajcf);
////			if ( f(lastAcceptedResult != null) && (lastReturnedResult.result != lastAcceptedResult)) {
////				// we've got everything we need for lastAcceptedResult, push the result out
////				// and free up the memory.
////				finishedWith(lastAcceptedResult);
////			}
////			lastAcceptedResult = lastReturnedResult.result;
//		}
//		
//		private void finishedWith(InterimResult result) {
//			compiler.requestor.acceptResult(result.result.tagAsAccepted());
//			for (int i = 0; i < compiler.unitsToProcess.length; i++) {
//				if (compiler.unitsToProcess[i] != null) {
//					if (compiler.unitsToProcess[i].compilationResult == result.result) {
//						compiler.unitsToProcess[i] = null;
//					}
//				}
//			}
//		}
//		
//		/* (non-Javadoc)
//		 * @see java.util.Iterator#hasNext()
//		 */
//		public boolean hasNext() {
//			if (nowProcessing == null) {
//				if (!resultIterator.hasNext()) return false;
//				nowProcessing = (InterimResult) resultIterator.next();
//				classFileIndex = 0;
//			}
//			while (nowProcessing.unwovenClassFiles.length == 0 ) {
//				if (!resultIterator.hasNext()) return false;
//				nowProcessing = (InterimResult) resultIterator.next();
//			}
//			if (classFileIndex < nowProcessing.unwovenClassFiles.length) {
//				return true;
//			} else {
//				classFileIndex = 0;
//				if (!resultIterator.hasNext()) return false;
//				nowProcessing = (InterimResult) resultIterator.next();
//				while (nowProcessing.unwovenClassFiles.length == 0 ) {
//					if (!resultIterator.hasNext()) return false;
//					nowProcessing = (InterimResult) resultIterator.next();
//				} 
//			}
//			return true;
//		}
//		/* (non-Javadoc)
//		 * @see java.util.Iterator#next()
//		 */
//		public Object next() {
//			if (!hasNext()) return null;  // sets up indices correctly
//			if (finalPhase) {
//				if ((lastReturnedResult != null) && (lastReturnedResult != nowProcessing)) {
//					// we're done with the lastReturnedResult
//					finishedWith(lastReturnedResult);
//				}
//			}
//			lastReturnedResult = nowProcessing;
//			compiler.wmHandler.setCurrentResult(nowProcessing.result);
//			return nowProcessing.unwovenClassFiles[classFileIndex++];
//		}
//		/* (non-Javadoc)
//		 * @see java.util.Iterator#remove()
//		 */
//		public void remove() {
//			throw new UnsupportedOperationException();
//		}
//		
//		public void processingReweavableState() {}
//		public void addingTypeMungers() {}
//		public void weavingAspects() {}
//		public void weavingClasses() {finalPhase = true;}
//		public void weaveCompleted() {
//			if ((lastReturnedResult != null) && (!lastReturnedResult.result.hasBeenAccepted)) {
//				finishedWith(lastReturnedResult);
//			}
//		}
//		
//		private void removeFromHashtable(Hashtable table, char[] key) {
//			// jdt uses char[] as a key in the hashtable, which is not very useful as equality is based on being
//			// the same array, not having the same content.
//			String skey = new String(key);
//			char[] victim = null;
//			for (Enumeration iter = table.keys(); iter.hasMoreElements();) {
//				char[] thisKey = (char[]) iter.nextElement();
//				if (skey.equals(new String(thisKey))) {
//					victim = thisKey;
//					break;
//				}
//			}
//			if (victim != null) {
//				table.remove(victim);
//			}
//		}
//}
//	
//	// Holder for intermediate form (class files)
//	// =======================================================================================
//	public static class InterimResult {
//	  public CompilationResult result;
//	  public UnwovenClassFile[] unwovenClassFiles;  // longer term would be nice not to have two copies of
//	                                                // the byte codes, one in result.classFiles and another
//	                                                // in unwovenClassFiles;
//	  private AjCompiler.IOutputClassFileNameProvider nameGen;
////	  public String[] classNames;  // entry at index i is the name of the class at unwovenClassFiles[i]
////	  public BcelObjectType[] classTypes; // entry at i is the resolved type of the class at unwovenClassFiles[i]
//	  public InterimResult(CompilationResult cr, AjCompiler.IOutputClassFileNameProvider np) {
//	  	result = cr;
//	  	nameGen = np;
//		unwovenClassFiles = ClassFileBasedByteCodeProvider.unwovenClassFilesFor(result,nameGen);
//	  }
//	  
//	  public String fileName() {
//	  	return new String(result.fileName);
//	  }
//	  
//      public boolean equals(Object other) {
//			if( other == null || !(other instanceof InterimResult)) {
//				return false;
//			}
//			InterimResult ir = (InterimResult) other;
//			return fileName().equals(ir.fileName());
//	  }
//	  public int hashCode() {
//			return fileName().hashCode();
//	  }
//	};
//	
//	
//	
//	// XXX lightweight subclass of ClassFile that only genuinely supports fileName and getBytes
//	// operations.
//	// =========================================================================================
//	private static class AjClassFile extends ClassFile {
//		
//		char[] filename;
//		byte[] bytes;
//	
//		public AjClassFile(char[] fileName, byte[] byteCodes) {
//			this.filename = fileName;
//			bytes = byteCodes;
//		}
//		
//		public char[] fileName() {
//			return filename;
//		}
//
//		public byte[] getBytes() {
//			return bytes;
//		}
//	};
//	
//	
//	// Adaptor for ClassFiles that lets them act as the bytecode repository
//	// for UnwovenClassFiles (asking a ClassFile for its bytes causes a 
//	// copy to be made).
//	private static class ClassFileBasedByteCodeProvider 
//	               implements UnwovenClassFileWithThirdPartyManagedBytecode.IByteCodeProvider {
//		private ClassFile cf;
//		
//		public ClassFileBasedByteCodeProvider(ClassFile cf) {
//			this.cf = cf;
//		}
//		
//		public byte[] getBytes() {
//			return cf.getBytes();
//		}
//		
//		public static UnwovenClassFile[] unwovenClassFilesFor(CompilationResult result, 
//											AjCompiler.IOutputClassFileNameProvider nameProvider) {
//			ClassFile[] cfs = result.getClassFiles();
//			UnwovenClassFile[] ret = new UnwovenClassFile[cfs.length];
//			for (int i = 0; i < ret.length; i++) {
//				ClassFileBasedByteCodeProvider p = new ClassFileBasedByteCodeProvider(cfs[i]);
//				String fileName = nameProvider.getOutputClassFileName(cfs[i].fileName(), result);
//				ret[i] = new UnwovenClassFileWithThirdPartyManagedBytecode(fileName,p);
//			}
//			return ret;
//		}
//		
//	}
//	
//	// Inner class for handling messages produced by the weaver
//	// ==========================================================
//	
//	private class WeaverMessageHandler implements IMessageHandler {
//		IMessageHandler sink;
//		CompilationResult currentlyWeaving;
//		
//		public WeaverMessageHandler(IMessageHandler handler) {
//			this.sink = handler;
//		}
//		
//		public void setCurrentResult(CompilationResult result) {
//			currentlyWeaving = result;
//		}
//
//		public boolean handleMessage(IMessage message) throws AbortException {
//			if (! (message.isError() || message.isWarning()) ) return sink.handleMessage(message);
//			// we only care about warnings and errors here...
//			ISourceLocation sLoc = message.getSourceLocation();
//			CompilationResult problemSource = currentlyWeaving;
//			if (problemSource == null) {
//				// must be a problem found during completeTypeBindings phase of begin to compile
//				if (sLoc instanceof EclipseSourceLocation) {
//					problemSource = ((EclipseSourceLocation)sLoc).getCompilationResult();
//				}
//				if (problemSource == null) {
//					// XXX this is ok for ajc, will have to do better for AJDT in time...
//					return sink.handleMessage(message);
//				}
//			}
//			int startPos = getStartPos(sLoc,problemSource);
//			int endPos = getEndPos(sLoc,problemSource);
//			int severity = message.isError() ? ProblemSeverities.Error : ProblemSeverities.Warning;
//			char[] filename = problemSource.fileName;
//			boolean usedBinarySourceFileName = false;
//			if (problemSource.isFromBinarySource()) {
//				if (sLoc != null) {
//					filename = sLoc.getSourceFile().getPath().toCharArray();
//					usedBinarySourceFileName = true;
//				}
//			}
//			ReferenceContext referenceContext = findReferenceContextFor(problemSource);
//			IProblem problem = problemReporter.createProblem(
//									filename,
//									IProblem.Unclassified,
//									new String[0],
//									new String[] {message.getMessage()},
//									severity,
//									startPos,
//									endPos,
//									sLoc != null ? sLoc.getLine() : 1,
//									referenceContext,
//									problemSource
//									);
//			IProblem[] seeAlso = buildSeeAlsoProblems(message.getExtraSourceLocations(),
//													  problemSource,	
//													  usedBinarySourceFileName);
//			problem.setSeeAlsoProblems(seeAlso);
//			if (message.getDetails() != null) {
//				problem.setSupplementaryMessageInfo(message.getDetails());
//			}
//			problemReporter.record(problem, problemSource, referenceContext);
//			return true;
////			if (weavingPhase) {
////				return sink.handleMessage(message); 
////			} else {
////				return true;  // message will be reported back in compilation result later...
////			}
//		}
//
//		public boolean isIgnoring(Kind kind) {
//			return sink.isIgnoring(kind);
//		}
//		
//		private int getStartPos(ISourceLocation sLoc,CompilationResult result) {
//			int pos = 0;
//			if (sLoc == null) return 0;
//			int line = sLoc.getLine();
//			if (sLoc instanceof EclipseSourceLocation) {
//				pos = ((EclipseSourceLocation)sLoc).getStartPos();
//			} else {
//				if (line <= 1) return 0;
//				if (result != null) {
//					if ((result.lineSeparatorPositions != null) && 
//						(result.lineSeparatorPositions.length >= (line-1))) {
//						pos = result.lineSeparatorPositions[line-2] + 1;
//					}
//				}
//			}
//			return pos;
//		}
//
//		private int getEndPos(ISourceLocation sLoc,CompilationResult result) {
//			int pos = 0;
//			if (sLoc == null) return 0;
//			int line = sLoc.getLine();
//			if (line <= 0) line = 1;
//			if (sLoc instanceof EclipseSourceLocation) {
//				pos = ((EclipseSourceLocation)sLoc).getEndPos();
//			} else {
//				if (result != null) {
//					if ((result.lineSeparatorPositions != null) && 
//						(result.lineSeparatorPositions.length >= line)) {
//						pos = result.lineSeparatorPositions[line -1] -1;
//					}
//				}
//			}
//			return pos;
//		}
//	
//		private ReferenceContext findReferenceContextFor(CompilationResult result) {
//			ReferenceContext context = null;
//			if (unitsToProcess == null) return null;
//			for (int i = 0; i < unitsToProcess.length; i++) {
//				if ((unitsToProcess[i] != null) &&
//				    (unitsToProcess[i].compilationResult == result)) {
//					context = unitsToProcess[i];
//					break;
//				}				
//			}
//			return context;
//		}
//		
//		private IProblem[] buildSeeAlsoProblems(List sourceLocations,
//												CompilationResult problemSource,
//												boolean usedBinarySourceFileName) {
//			int probLength = sourceLocations.size();
//			if (usedBinarySourceFileName) probLength++;
//			IProblem[] ret = new IProblem[probLength];
//			for (int i = 0; i < sourceLocations.size(); i++) {
//				ISourceLocation loc = (ISourceLocation) sourceLocations.get(i);
//				ret[i] = new DefaultProblem(loc.getSourceFile().getPath().toCharArray(),
//											"see also",
//											0,
//											new String[] {},
//											ProblemSeverities.Ignore,
//											getStartPos(loc,null),
//											getEndPos(loc,null),
//											loc.getLine());
//			}
//			if (usedBinarySourceFileName) {
//				ret[ret.length -1] = new DefaultProblem(problemSource.fileName,"see also",0,new String[] {},
//														ProblemSeverities.Ignore,0,
//														0,0);
//			}
//			return ret;
//		}
//	}
//	
//}
