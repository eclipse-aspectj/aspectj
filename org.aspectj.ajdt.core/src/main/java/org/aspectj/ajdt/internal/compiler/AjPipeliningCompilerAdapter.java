/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Andy Clement    - initial implementation 26Jul06
 *******************************************************************************/
package org.aspectj.ajdt.internal.compiler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.aspectj.ajdt.internal.compiler.ast.AddAtAspectJAnnotationsVisitor;
import org.aspectj.ajdt.internal.compiler.ast.AspectDeclaration;
import org.aspectj.ajdt.internal.compiler.ast.InterTypeConstructorDeclaration;
import org.aspectj.ajdt.internal.compiler.ast.InterTypeFieldDeclaration;
import org.aspectj.ajdt.internal.compiler.ast.InterTypeMethodDeclaration;
import org.aspectj.ajdt.internal.compiler.ast.ValidateAtAspectJAnnotationsVisitor;
import org.aspectj.ajdt.internal.compiler.lookup.EclipseFactory;
import org.aspectj.ajdt.internal.core.builder.AjState;
import org.aspectj.asm.internal.CharOperation;
import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.IMessageHandler;
import org.aspectj.bridge.IProgressListener;
import org.aspectj.bridge.context.CompilationAndWeavingContext;
import org.aspectj.bridge.context.ContextToken;
import org.aspectj.org.eclipse.jdt.internal.compiler.CompilationResult;
import org.aspectj.org.eclipse.jdt.internal.compiler.Compiler;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.aspectj.org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.aspectj.org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.aspectj.org.eclipse.jdt.internal.compiler.problem.AbortCompilation;
import org.aspectj.weaver.bcel.BcelWeaver;
import org.aspectj.weaver.bcel.BcelWorld;
import org.aspectj.weaver.bcel.UnwovenClassFile;

/**
 * Adapts standard JDT Compiler to add in AspectJ specific behaviours. This version implements pipelining - where files are compiled
 * and then woven immediately, unlike AjCompilerAdapter which compiles everything then weaves everything. (One small note: because
 * all aspects have to be known before weaving can take place, the weaving pipeline is 'stalled' until all aspects have been
 * compiled).
 * 
 * The basic strategy is this:
 * 
 * 1. diet parse all input source files - this is enough for us to implement ITD matching - this enables us to determine which are
 * aspects 2. sort the input files, aspects first - keep a note of how many files contain aspects 3. if there are aspects, mark the
 * pipeline as 'stalled' 3. repeat 3a. compile a file 3b. have we now compiled all aspects? NO - put file in a weave pending queue
 * YES- unstall the 'pipeline' 3c. is the pipeline stalled? NO - weave all pending files and this one YES- do nothing
 * 
 * Complexities arise because of: - what does -XterminateAfterCompilation mean? since there is no stage where everything is compiled
 * and nothing is woven
 * 
 * 
 * Here is the compiler loop difference when pipelining.
 * 
 * the old way: Finished diet parsing [C:\temp\ajcSandbox\aspectjhead\ajcTest23160.tmp\ClassOne.java] Finished diet parsing
 * [C:\temp\ajcSandbox\aspectjhead\ajcTest23160.tmp\ClassTwo.java] &gt; AjLookupEnvironment.completeTypeBindings() &lt;
 * AjLookupEnvironment.completeTypeBindings() compiling C:\temp\ajcSandbox\aspectjhead\ajcTest23160.tmp\ClassOne.java
 * &gt;Compiler.process(C:\temp\ajcSandbox\aspectjhead\ajcTest23160.tmp\ClassOne.java)
 * &lt;Compiler.process(C:\temp\ajcSandbox\aspectjhead\ajcTest23160.tmp\ClassOne.java) compiling
 * C:\temp\ajcSandbox\aspectjhead\ajcTest23160.tmp\ClassTwo.java
 * &gt;Compiler.process(C:\temp\ajcSandbox\aspectjhead\ajcTest23160.tmp\ClassTwo.java)
 * &lt;Compiler.process(C:\temp\ajcSandbox\aspectjhead\ajcTest23160.tmp\ClassTwo.java) &gt;AjCompilerAdapter.weave()
 * &gt;BcelWeaver.prepareForWeave &lt;BcelWeaver.prepareForWeave woven class ClassOne (from
 * C:\temp\ajcSandbox\aspectjhead\ajcTest23160.tmp\ClassOne.java) woven class ClassTwo (from
 * C:\temp\ajcSandbox\aspectjhead\ajcTest23160.tmp\ClassTwo.java) &lt;AjCompilerAdapter.weave()
 * 
 * the new way (see the compiling/weaving mixed up): Finished diet parsing
 * [C:\temp\ajcSandbox\aspectjhead\ajcTest23160.tmp\ClassOne.java] Finished diet parsing
 * [C:\temp\ajcSandbox\aspectjhead\ajcTest23160.tmp\ClassTwo.java] &gt;AjLookupEnvironment.completeTypeBindings()
 * &lt;AjLookupEnvironment.completeTypeBindings() compiling C:\temp\ajcSandbox\aspectjhead\ajcTest23160.tmp\ClassOne.java
 * &gt;Compiler.process(C:\temp\ajcSandbox\aspectjhead\ajcTest23160.tmp\ClassOne.java)
 * &lt;Compiler.process(C:\temp\ajcSandbox\aspectjhead\ajcTest23160.tmp\ClassOne.java) &gt;AjCompilerAdapter.weave()
 * &gt;BcelWeaver.prepareForWeave &lt;BcelWeaver.prepareForWeave woven class ClassOne (from
 * C:\temp\ajcSandbox\aspectjhead\ajcTest23160.tmp\ClassOne.java) &lt;AjCompilerAdapter.weave() compiling
 * C:\temp\ajcSandbox\aspectjhead\ajcTest23160.tmp\ClassTwo.java
 * &gt;Compiler.process(C:\temp\ajcSandbox\aspectjhead\ajcTest23160.tmp\ClassTwo.java)
 * &lt;Compiler.process(C:\temp\ajcSandbox\aspectjhead\ajcTest23160.tmp\ClassTwo.java) &gt;AjCompilerAdapter.weave() woven class ClassTwo
 * (from C:\temp\ajcSandbox\aspectjhead\ajcTest23160.tmp\ClassTwo.java) <AjCompilerAdapter.weave()
 * 
 * 
 */
public class AjPipeliningCompilerAdapter extends AbstractCompilerAdapter {

	private Compiler compiler;
	private BcelWeaver weaver;
	private EclipseFactory eWorld;
	private boolean isBatchCompile;
	private boolean reportedErrors;
	private boolean isXTerminateAfterCompilation;
	private boolean proceedOnError;
	private boolean inJava5Mode;
	private boolean makeReflectable;
	private boolean noAtAspectJAnnotationProcessing;
	private IIntermediateResultsRequestor intermediateResultsRequestor;
	private IProgressListener progressListener;
	private IOutputClassFileNameProvider outputFileNameProvider;
	private IBinarySourceProvider binarySourceProvider;
	private WeaverMessageHandler weaverMessageHandler;
	private Map<String, List<UnwovenClassFile>> binarySourceSetForFullWeave = new HashMap<>();

	private ContextToken processingToken = null;
	private ContextToken resolvingToken = null;
	private ContextToken analysingToken = null;
	private ContextToken generatingToken = null;

	private AjState incrementalCompilationState;

	// Maintains a list of whats weaving - whilst the pipeline is stalled, this accumulates aspects.
	List<InterimCompilationResult> resultsPendingWeave = new ArrayList<>();

	// pipelining info
	private boolean pipelineStalled = true;
	private boolean weaverInitialized = false;
	private int toWaitFor;
	// If we determine we are going to drop back to a full build - don't need to tell the weaver to report adviceDidNotMatch
	private boolean droppingBackToFullBuild;

	/**
	 * Create an adapter, and tell it everything it needs to now to drive the AspectJ parts of a compile cycle.
	 * 
	 * @param compiler the JDT compiler that produces class files from source
	 * @param isBatchCompile true if this is a full build (non-incremental)
	 * @param world the bcelWorld used for type resolution during weaving
	 * @param weaver the weaver
	 * @param intRequestor recipient of interim compilation results from compiler (pre-weave)
	 * @param outputFileNameProvider implementor of a strategy providing output file names for results
	 * @param binarySourceProvider binary source that we didn't compile, but that we need to weave
	 * @param incrementalCompilationState if we are doing an incremental build, and the weaver determines that we need to weave the world,
	 *        this is the set of intermediate results that will be passed to the weaver.
	 */
	public AjPipeliningCompilerAdapter(Compiler compiler, boolean isBatchCompile, BcelWorld world, BcelWeaver weaver,
			EclipseFactory eFactory, IIntermediateResultsRequestor intRequestor, IProgressListener progressListener,
			IOutputClassFileNameProvider outputFileNameProvider, IBinarySourceProvider binarySourceProvider,
			Map fullBinarySourceEntries, /* fileName |-> List<UnwovenClassFile> */
			boolean isXterminateAfterCompilation, boolean proceedOnError, boolean noAtAspectJProcessing, boolean makeReflectable,
			AjState incrementalCompilationState) {
		this.compiler = compiler;
		this.isBatchCompile = isBatchCompile;
		this.weaver = weaver;
		this.intermediateResultsRequestor = intRequestor;
		this.progressListener = progressListener;
		this.outputFileNameProvider = outputFileNameProvider;
		this.binarySourceProvider = binarySourceProvider;
		this.isXTerminateAfterCompilation = isXterminateAfterCompilation;
		this.proceedOnError = proceedOnError;
		this.binarySourceSetForFullWeave = fullBinarySourceEntries;
		this.eWorld = eFactory;
		this.inJava5Mode = false;
		this.makeReflectable = makeReflectable;
		this.noAtAspectJAnnotationProcessing = noAtAspectJProcessing;
		this.incrementalCompilationState = incrementalCompilationState;

		if (compiler.options.complianceLevel >= ClassFileConstants.JDK1_5) {
			inJava5Mode = true;
		}
		IMessageHandler msgHandler = world.getMessageHandler();
		// Do we need to reset the message handler or create a new one? (This saves a ton of memory lost on incremental compiles...)
		if (msgHandler instanceof WeaverMessageHandler) {
			((WeaverMessageHandler) msgHandler).resetCompiler(compiler);
			weaverMessageHandler = (WeaverMessageHandler) msgHandler;
		} else {
			weaverMessageHandler = new WeaverMessageHandler(msgHandler, compiler);
			world.setMessageHandler(weaverMessageHandler);
		}
	}

	// the compilation lifecycle methods below are called in order as compilation progresses...

	/**
	 * In a pipelining compilation system, we need to ensure aspects are through the pipeline first. Only when they are all through
	 * (and therefore we know about all static/dynamic crosscutting) can be proceed to weave anything. Effectively the weaving part
	 * of the pipeline stalls until all the aspects have been fully compiled. This method sorts the compilation units such that any
	 * containing aspects are fully compiled first and it keeps a note on how long it should stall the pipeline before commencing
	 * weaving.
	 */
	public void afterDietParsing(CompilationUnitDeclaration[] units) {
		if (debugPipeline) {
			System.err.println("> afterDietParsing: there are " + (units == null ? 0 : units.length) + " units to sort");
		}

		if (!reportedErrors && units != null) {
			for (CompilationUnitDeclaration unit : units) {
				if (unit != null && unit.compilationResult != null && unit.compilationResult.hasErrors()) {
					reportedErrors = true;
					break; // TODO break or exit here?
				}
			}
		}

		// Break the units into two lists...
		List<CompilationUnitDeclaration> aspects = new ArrayList<>();
		List<CompilationUnitDeclaration> nonaspects = new ArrayList<>();
		for (CompilationUnitDeclaration unit : units) {
			if (containsAnAspect(unit)) {
				aspects.add(unit);
			} else {
				nonaspects.add(unit);
			}
		}

		// ...and put them back together, aspects first
		int posn = 0;
		for (CompilationUnitDeclaration aspect : aspects) {
			units[posn++] = aspect;
		}
		for (CompilationUnitDeclaration nonaspect : nonaspects) {
			units[posn++] = nonaspect;
		}

		// Work out how long to stall the pipeline
		toWaitFor = aspects.size();
		if (debugPipeline) {
			System.err.println("< afterDietParsing: stalling pipeline for " + toWaitFor + " source files");
		}

		// TESTING
		if (pipelineTesting) {
			if (pipelineOutput == null) {
				pipelineOutput = new Hashtable();
			}
			pipelineOutput.put("filesContainingAspects", new Integer(toWaitFor).toString());
			StringBuffer order = new StringBuffer();
			order.append("[");
			for (int i = 0; i < units.length; i++) {
				if (i != 0) {
					order.append(",");
				}
				CompilationUnitDeclaration declaration = units[i];
				String filename = new String(declaration.getFileName());
				int idx = filename.lastIndexOf('/');
				if (idx > 0) {
					filename = filename.substring(idx + 1);
				}
				idx = filename.lastIndexOf('\\');
				if (idx > 0) {
					filename = filename.substring(idx + 1);
				}
				order.append(filename);
			}
			order.append("]");
			pipelineOutput.put("weaveOrder", order.toString());
		}
	}

	public void beforeCompiling(ICompilationUnit[] sourceUnits) {
		resultsPendingWeave = new ArrayList<>();
		reportedErrors = false;
		droppingBackToFullBuild = false;
	}

	public void beforeProcessing(CompilationUnitDeclaration unit) {
		if (debugPipeline) {
			System.err.println("compiling " + new String(unit.getFileName()));
		}
		eWorld.showMessage(IMessage.INFO, "compiling " + new String(unit.getFileName()), null, null);
		processingToken = CompilationAndWeavingContext.enteringPhase(CompilationAndWeavingContext.PROCESSING_COMPILATION_UNIT, unit
				.getFileName());
		if (inJava5Mode && !noAtAspectJAnnotationProcessing) {
			ContextToken tok = CompilationAndWeavingContext.enteringPhase(
					CompilationAndWeavingContext.ADDING_AT_ASPECTJ_ANNOTATIONS, unit.getFileName());
			AddAtAspectJAnnotationsVisitor atAspectJVisitor = new AddAtAspectJAnnotationsVisitor(unit, makeReflectable);
			unit.traverse(atAspectJVisitor, unit.scope);
			CompilationAndWeavingContext.leavingPhase(tok);
		}
	}

	public void beforeResolving(CompilationUnitDeclaration unit) {
		resolvingToken = CompilationAndWeavingContext.enteringPhase(CompilationAndWeavingContext.RESOLVING_COMPILATION_UNIT, unit
				.getFileName());
	}

	public void afterResolving(CompilationUnitDeclaration unit) {
		if (resolvingToken != null) {
			CompilationAndWeavingContext.leavingPhase(resolvingToken);
		}
	}

	public void beforeAnalysing(CompilationUnitDeclaration unit) {
		analysingToken = CompilationAndWeavingContext.enteringPhase(CompilationAndWeavingContext.ANALYSING_COMPILATION_UNIT, unit
				.getFileName());
		if (inJava5Mode && !noAtAspectJAnnotationProcessing) {
			ValidateAtAspectJAnnotationsVisitor atAspectJVisitor = new ValidateAtAspectJAnnotationsVisitor(unit);
			unit.traverse(atAspectJVisitor, unit.scope);
		}
	}

	public void afterAnalysing(CompilationUnitDeclaration unit) {
		if (analysingToken != null) {
			CompilationAndWeavingContext.leavingPhase(analysingToken);
		}
	}

	public void beforeGenerating(CompilationUnitDeclaration unit) {
		generatingToken = CompilationAndWeavingContext.enteringPhase(
				CompilationAndWeavingContext.GENERATING_UNWOVEN_CODE_FOR_COMPILATION_UNIT, unit.getFileName());
		if (eWorld.pushinCollector != null) {
			if (unit.types != null && unit.types.length > 0) {
				for (int t = 0; t < unit.types.length; t++) {
					TypeDeclaration type = unit.types[t];
					if (type.methods != null) {
						for (int m = 0; m < type.methods.length; m++) {
							AbstractMethodDeclaration md = type.methods[m];
							if (md instanceof InterTypeMethodDeclaration) {
								InterTypeMethodDeclaration itmd = ((InterTypeMethodDeclaration) md);
								ITDMethodPrinter printer = new ITDMethodPrinter(itmd, md.scope);
								String s = printer.print();
								eWorld.pushinCollector.recordInterTypeMethodDeclarationCode(md, s, getDeclarationLineNumber(md));
							} else if (md instanceof InterTypeFieldDeclaration) {
								ITDFieldPrinter printer = new ITDFieldPrinter(((InterTypeFieldDeclaration) md), md.scope);
								String s = printer.print();
								eWorld.pushinCollector.recordInterTypeFieldDeclarationCode(md, s, getDeclarationLineNumber(md));
							} else if (md instanceof InterTypeConstructorDeclaration) {
								ITDConstructorPrinter printer = new ITDConstructorPrinter(((InterTypeConstructorDeclaration) md),
										md.scope);
								String s = printer.print();
								eWorld.pushinCollector.recordInterTypeConstructorDeclarationCode(md, s,
										getDeclarationLineNumber(md));
								// } else if (md instanceof DeclareAnnotationDeclaration) {
								// DeclareAnnotationDeclaration dad = (DeclareAnnotationDeclaration) md;
								// String value = new DeclareAnnotationsPrinter(dad, dad.scope).print();
								// eWorld.pushinCollector.recordDeclareAnnotationDeclarationCode(md, value);
							}
						}
					}
				}
			}
			eWorld.pushinCollector.setOutputFileNameProvider(outputFileNameProvider);
		}
	}

	/**
	 * @return the line number for this declaration in the source code
	 */
	private int getDeclarationLineNumber(AbstractMethodDeclaration md) {
		int sourceStart = md.sourceStart;
		int[] separators = md.compilationResult.lineSeparatorPositions;
		int declarationStartLine = 1;
		for (int separator : separators) {
			if (sourceStart < separator) {
				break;
			}
			declarationStartLine++;
		}
		return declarationStartLine;
	}

	public void afterGenerating(CompilationUnitDeclaration unit) {
		if (generatingToken != null) {
			CompilationAndWeavingContext.leavingPhase(generatingToken);
		}
		if (eWorld.pushinCollector != null) {
			eWorld.pushinCollector.dump(unit);
		}
	}

	public void afterCompiling(CompilationUnitDeclaration[] units) {
		this.eWorld.cleanup();
		if (!weaverInitialized) { // nothing got compiled, doesnt mean there is nothing to do... (binary weaving)
			if (!(isXTerminateAfterCompilation || (reportedErrors && !proceedOnError))) {
				// acceptResult(unit.compilationResult);
				// } else {
				try {
					if (weaveQueuedEntries()) {
						droppingBackToFullBuild = true;
					}
				} catch (IOException ex) {
					AbortCompilation ac = new AbortCompilation(null, ex);
					throw ac;
				}
			}
		}
		postWeave();
		try {
			// not great ... but one more check before we continue, see pr132314
			if (!reportedErrors && units != null) {
				for (CompilationUnitDeclaration unit : units) {
					if (unit != null && unit.compilationResult != null && unit.compilationResult.hasErrors()) {
						reportedErrors = true;
						break;
					}
				}
			}
			if (isXTerminateAfterCompilation || (reportedErrors && !proceedOnError)) {
				// no point weaving... just tell the requestor we're done
				notifyRequestor();
			} else {
				// weave(); // notification happens as weave progresses...
				// weaver.getWorld().flush(); // pr152257
			}
			// } catch (IOException ex) {
			// AbortCompilation ac = new AbortCompilation(null,ex);
			// throw ac;
		} catch (RuntimeException rEx) {
			if (rEx instanceof AbortCompilation) {
				throw rEx; // Don't wrap AbortCompilation exceptions!
			}

			// This will be unwrapped in Compiler.handleInternalException() and the nested
			// RuntimeException thrown back to the original caller - which is AspectJ
			// which will then then log it as a compiler problem.
			throw new AbortCompilation(true, rEx);
		}
	}

	public void afterProcessing(CompilationUnitDeclaration unit, int unitIndex) {
		CompilationAndWeavingContext.leavingPhase(processingToken);
		eWorld.finishedCompilationUnit(unit);
		InterimCompilationResult intRes = new InterimCompilationResult(unit.compilationResult, outputFileNameProvider);
		if (unit.compilationResult.hasErrors()) {
			reportedErrors = true;
		}

		if (intermediateResultsRequestor != null) {
			intermediateResultsRequestor.acceptResult(intRes);
		}

		if (unit.compilationResult.hasErrors() || (isXTerminateAfterCompilation || (reportedErrors && !proceedOnError))) {
			acceptResult(unit.compilationResult);
		} else {
			queueForWeaving(intRes);
		}
	}

	private void queueForWeaving(InterimCompilationResult intRes) {
		resultsPendingWeave.add(intRes);
		if (pipelineStalled) {
			if (resultsPendingWeave.size() >= toWaitFor) {
				pipelineStalled = false;
			}
		}
		if (pipelineStalled) {
			return;
		}
		try {
			if (weaveQueuedEntries()) {
				droppingBackToFullBuild = true;
			}
		} catch (IOException ex) {
			AbortCompilation ac = new AbortCompilation(null, ex);
			throw ac;
		}
	}

	/*
	 * Called from the weaverAdapter once it has finished weaving the class files associated with a given compilation result.
	 */
	public void acceptResult(CompilationResult result) {
		compiler.requestor.acceptResult(result.tagAsAccepted());
		if (compiler.unitsToProcess != null) {
			for (int i = 0; i < compiler.unitsToProcess.length; i++) {
				if (compiler.unitsToProcess[i] != null) {
					if (compiler.unitsToProcess[i].compilationResult == result) {
						compiler.unitsToProcess[i].cleanUp();
						compiler.unitsToProcess[i] = null;
					}
				}
			}
		}
	}

	// helper methods...
	// ==================================================================================

	private List<InterimCompilationResult> getBinarySourcesFrom(Map<String, List<UnwovenClassFile>> binarySourceEntries) {
		// Map is fileName |-> List<UnwovenClassFile>
		List<InterimCompilationResult> ret = new ArrayList<>();
		for (String sourceFileName : binarySourceEntries.keySet()) {
			List<UnwovenClassFile> unwovenClassFiles = binarySourceEntries.get(sourceFileName);
			// XXX - see bugs 57432,58679 - final parameter on next call should be "compiler.options.maxProblemsPerUnit"
			CompilationResult result = new CompilationResult(sourceFileName.toCharArray(), 0, 0, Integer.MAX_VALUE);
			result.noSourceAvailable();
			InterimCompilationResult binarySource = new InterimCompilationResult(result, unwovenClassFiles);
			ret.add(binarySource);
		}
		return ret;
	}

	private void notifyRequestor() {
		for (InterimCompilationResult iresult : resultsPendingWeave) {
			compiler.requestor.acceptResult(iresult.result().tagAsAccepted());
		}
	}

	/** Return true if we've decided to drop back to a full build (too much has changed) */
	private boolean weaveQueuedEntries() throws IOException {
		if (debugPipeline) {
			System.err.println(">.weaveQueuedEntries()");
		}
		for (InterimCompilationResult iresult : resultsPendingWeave) {
			for (int i = 0; i < iresult.unwovenClassFiles().length; i++) {
				weaver.addClassFile(iresult.unwovenClassFiles()[i], false);
			}
		}
		ensureWeaverInitialized(); // by doing this only once, are we saying needToReweaveWorld can't change once the aspects have
		// been stuffed into the weaver?
		if (weaver.needToReweaveWorld() && !isBatchCompile) {
			return true;
		}
		weaver.weave(new WeaverAdapter(this, weaverMessageHandler, progressListener));
		resultsPendingWeave.clear(); // dont need to do those again
		this.eWorld.minicleanup();
		if (debugPipeline) {
			System.err.println("<.weaveQueuedEntries()");
		}
		return false;
	}

	private void ensureWeaverInitialized() {
		if (weaverInitialized) {
			return;
		}
		weaverInitialized = true;
		weaver.setIsBatchWeave(isBatchCompile);
		weaver.prepareForWeave();
		if (weaver.needToReweaveWorld()) {
			if (!isBatchCompile) {
				// force full recompilation from source
				this.incrementalCompilationState.forceBatchBuildNextTimeAround();
				return;
			}
			resultsPendingWeave.addAll(getBinarySourcesFrom(binarySourceSetForFullWeave));
		} else {
			Map binarySourcesToAdd = binarySourceProvider.getBinarySourcesForThisWeave();
			resultsPendingWeave.addAll(getBinarySourcesFrom(binarySourcesToAdd));
		}
	}

	// private void weave() throws IOException {
	// if (debugPipeline)System.err.println("> weave()");
	// // ensure weaver state is set up correctly
	// for (Iterator iter = resultsPendingWeave.iterator(); iter.hasNext();) {
	// InterimCompilationResult iresult = (InterimCompilationResult) iter.next();
	// for (int i = 0; i < iresult.unwovenClassFiles().length; i++) {
	// weaver.addClassFile(iresult.unwovenClassFiles()[i]);
	// }
	// }
	//
	// weaver.setIsBatchWeave(isBatchCompile);
	// weaver.prepareForWeave();
	// if (weaver.needToReweaveWorld()) {
	// if (!isBatchCompile) {
	// //force full recompilation from source
	// this.incrementalCompilationState.forceBatchBuildNextTimeAround();
	// return;
	// }
	// resultsPendingWeave.addAll(getBinarySourcesFrom(binarySourceSetForFullWeave));
	// } else {
	// Map binarySourcesToAdd = binarySourceProvider.getBinarySourcesForThisWeave();
	// resultsPendingWeave.addAll(getBinarySourcesFrom(binarySourcesToAdd));
	// }
	//
	// try {
	// weaver.weave(new WeaverAdapter(this,weaverMessageHandler,progressListener));
	// } finally {
	// weaver.tidyUp();
	// IMessageHandler imh = weaver.getWorld().getMessageHandler();
	// if (imh instanceof WeaverMessageHandler)
	// ((WeaverMessageHandler)imh).resetCompiler(null);
	// }
	// if (debugPipeline)System.err.println("< weave()");
	// }

	private void postWeave() {
		if (debugPipeline) {
			System.err.println("> postWeave()");
		}
		IMessageHandler imh = weaver.getWorld().getMessageHandler();
		if (imh instanceof WeaverMessageHandler) {
			((WeaverMessageHandler) imh).setCurrentResult(null);
		}
		if (!droppingBackToFullBuild) {
			weaver.allWeavingComplete();
		}
		weaver.tidyUp();
		if (imh instanceof WeaverMessageHandler) {
			((WeaverMessageHandler) imh).resetCompiler(null);
		}
		if (debugPipeline) {
			System.err.println("< postWeave()");
		}
	}

	/**
	 * Return true if the compilation unit declaration contains an aspect declaration (either code style or annotation style). It
	 * must inspect the multiple types that may be in a compilation unit declaration and any inner types.
	 */
	private boolean containsAnAspect(CompilationUnitDeclaration cud) {
		TypeDeclaration[] typeDecls = cud.types;
		if (typeDecls != null) {
			for (TypeDeclaration declaration : typeDecls) { // loop through top level types in the file
				if (isAspect(declaration)) {
					return true;
				}
				if (declaration.memberTypes != null) {
					TypeDeclaration[] memberTypes = declaration.memberTypes;
					for (TypeDeclaration memberType : memberTypes) { // loop through inner types
						if (containsAnAspect(memberType)) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	private boolean containsAnAspect(TypeDeclaration tDecl) {
		if (isAspect(tDecl)) {
			return true;
		}
		if (tDecl.memberTypes != null) {
			TypeDeclaration[] memberTypes = tDecl.memberTypes;
			for (TypeDeclaration memberType : memberTypes) { // loop through inner types
				if (containsAnAspect(memberType)) {
					return true;
				}
			}
		}
		return false;
	}

	private static final char[] aspectSig = "Lorg/aspectj/lang/annotation/Aspect;".toCharArray();

	private boolean isAspect(TypeDeclaration declaration) {
		// avoid an NPE when something else is wrong in this system ... the real problem will be reported elsewhere
		if (declaration.staticInitializerScope == null) {
			return false;
		}
		if (declaration instanceof AspectDeclaration) {
			return true; // code style
		} else if (declaration.annotations != null) { // check for annotation style
			for (int index = 0; index < declaration.annotations.length; index++) {
				// Cause annotation resolution
				declaration.binding.getAnnotationTagBits();
				Annotation a = declaration.annotations[index];
				if (a.resolvedType == null) {
					continue; // another problem is being reported, so don't crash here
				}
				if (CharOperation.equals(a.resolvedType.signature(), aspectSig)) {
					return true;
				}
			}
		}
		return false;
	}

	// ---
	/**
	 * SECRET: FOR TESTING - this can be used to collect information that tests can verify.
	 */
	public static boolean pipelineTesting = false;
	public static Hashtable<String, String> pipelineOutput = null;

	// Keys into pipelineOutput:
	// compileOrder "[XXX,YYY]" a list of the order in which files will be woven (aspects should be first)
	// filesContainingAspects "NNN" how many input source files have aspects inside
	// 

	public static String getPipelineDebugOutput(String key) {
		if (pipelineOutput == null) {
			return "";
		}
		return pipelineOutput.get(key);
	}

	private final static boolean debugPipeline = false;

	public List<InterimCompilationResult> getResultsPendingWeave() {
		return resultsPendingWeave;
	}

}
