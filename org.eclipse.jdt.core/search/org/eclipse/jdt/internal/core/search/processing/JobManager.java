/*******************************************************************************
 * Copyright (c) 2000, 2001, 2002 International Business Machines Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.jdt.internal.core.search.processing;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.internal.core.search.Util;

public abstract class JobManager implements Runnable {

	/* queue of jobs to execute */
	protected IJob[] awaitingJobs = new IJob[10];
	protected int jobStart = 0;
	protected int jobEnd = -1;
	protected boolean executing = false;

	/* background processing */
	protected Thread thread;

	/* flag indicating whether job execution is enabled or not */
	private boolean enabled = true;

	public static boolean VERBOSE = false;
	/* flag indicating that the activation has completed */
	public boolean activated = false;
	
	private int awaitingClients = 0;

	public static void verbose(String log) {
		System.out.println("(" + Thread.currentThread() + ") " + log); //$NON-NLS-1$//$NON-NLS-2$
	}

	/**
	 * Invoked exactly once, in background, before starting processing any job
	 */
	public void activateProcessing() {
		this.activated = true;
	}
	/**
	 * Answer the amount of awaiting jobs.
	 */
	public synchronized int awaitingJobsCount() {

		// pretend busy in case concurrent job attempts performing before activated
		if (!activated)
			return 1;

		return jobEnd - jobStart + 1;

	}
	/**
	 * Answers the first job in the queue, or null if there is no job available
	 * Until the job has completed, the job manager will keep answering the same job.
	 */
	public synchronized IJob currentJob() {

		if (!enabled)
			return null;

		if (jobStart <= jobEnd) {
			return awaitingJobs[jobStart];
		}
		return null;
	}
	public synchronized void disable() {
		enabled = false;
	}
	/**
	 * Remove the index from cache for a given project.
	 * Passing null as a job family discards them all.
	 */
	public void discardJobs(String jobFamily) {
		boolean wasEnabled = isEnabled();
		try {
			IJob currentJob;
			// cancel current job if it belongs to the given family
			synchronized(this){
				currentJob = this.currentJob();
				disable();
			}
			if (currentJob != null 
					&& (jobFamily == null || currentJob.belongsTo(jobFamily))) {
	
				currentJob.cancel();
			
				// wait until current active job has finished
				while (thread != null && executing){
					try {
						Thread.currentThread().sleep(50);
					} catch(InterruptedException e){
					}
				}
			}
	
			// flush and compact awaiting jobs
			int loc = -1;
			synchronized(this) {
				for (int i = jobStart; i <= jobEnd; i++) {
					currentJob = awaitingJobs[i];
					awaitingJobs[i] = null;
					if (!(jobFamily == null
						|| currentJob.belongsTo(jobFamily))) { // copy down, compacting
						awaitingJobs[++loc] = currentJob;
					} else {
						currentJob.cancel();
					}
				}
				jobStart = 0;
				jobEnd = loc;
			}
		} finally {
			if (wasEnabled)
				enable();
		}
	}
	public synchronized void enable() {
		enabled = true;
	}
	public synchronized boolean isEnabled() {
		return enabled;
	}
	/**
	 * Advance to the next available job, once the current one has been completed.
	 * Note: clients awaiting until the job count is zero are still waiting at this point.
	 */
	protected synchronized void moveToNextJob() {

		//if (!enabled) return;

		if (jobStart <= jobEnd) {
			awaitingJobs[jobStart++] = null;
			if (jobStart > jobEnd) {
				jobStart = 0;
				jobEnd = -1;
			}
		}
	}
	/**
	 * When idle, give chance to do something
	 */
	protected void notifyIdle(long idlingTime) {
	}
	/**
	 * This API is allowing to run one job in concurrence with background processing.
	 * Indeed since other jobs are performed in background, resource sharing might be 
	 * an issue.Therefore, this functionality allows a given job to be run without
	 * colliding with background ones.
	 * Note: multiple thread might attempt to perform concurrent jobs at the same time,
	 * 			and shoud synchronize (it is deliberately left to clients to decide whether
	 *			concurrent jobs might interfere or not, i.e. multiple read jobs are ok).
	 *
	 * Waiting policy can be:
	 * 		IJobConstants.ForceImmediateSearch
	 * 		IJobConstants.CancelIfNotReadyToSearch
	 * 		IJobConstants.WaitUntilReadyToSearch
	 *
	 */
	public boolean performConcurrentJob(
		IJob searchJob,
		int waitingPolicy,
		IProgressMonitor progress) {

		if (VERBOSE)
			JobManager.verbose("STARTING concurrent job - " + searchJob); //$NON-NLS-1$
		int concurrentJobWork = 100;
		if (progress != null) {
			progress.beginTask("", concurrentJobWork); //$NON-NLS-1$
		}
		boolean status = IJob.FAILED;
		if (awaitingJobsCount() > 0) {
			switch (waitingPolicy) {

				case IJob.ForceImmediate :
					if (VERBOSE)
						JobManager.verbose("-> NOT READY - Forcing immediate - " + searchJob);//$NON-NLS-1$
					boolean wasEnabled = isEnabled();
					try {
						disable(); // pause indexing
						status = searchJob.execute(progress == null ? null : new SubProgressMonitor(progress, concurrentJobWork));
					} finally {
						if (wasEnabled)
							enable();
					}
					if (VERBOSE)
						JobManager.verbose("DONE concurrent job - " + searchJob); //$NON-NLS-1$
					return status;
				case IJob.CancelIfNotReady :
					if (VERBOSE)
						JobManager.verbose("-> NOT READY - Cancelling - " + searchJob); //$NON-NLS-1$
					progress.setCanceled(true);
					if (VERBOSE)
						JobManager.verbose("CANCELLED concurrent job - " + searchJob); //$NON-NLS-1$
					break;

				case IJob.WaitUntilReady :
					int awaitingWork;
					IJob previousJob = null;
					IJob currentJob;
					IProgressMonitor subProgress = null;
					int totalWork = this.awaitingJobsCount();
					if (progress != null && totalWork > 0) {
						subProgress = new SubProgressMonitor(progress, concurrentJobWork / 2);
						subProgress.beginTask("", totalWork); //$NON-NLS-1$
						concurrentJobWork = concurrentJobWork / 2;
					}
					int originalPriority = this.thread.getPriority();
					try {
						synchronized(this) {
							if (this.thread != null) {
								this.thread.setPriority(Thread.currentThread().getPriority());
							}
							this.awaitingClients++;
						}
						while ((awaitingWork = awaitingJobsCount()) > 0) {
							if (subProgress != null && subProgress.isCanceled())
								throw new OperationCanceledException();
							currentJob = currentJob();
							// currentJob can be null when jobs have been added to the queue but job manager is not enabled
							if (currentJob != null && currentJob != previousJob) {
								if (VERBOSE)
									JobManager.verbose("-> NOT READY - Waiting until ready - " + searchJob);//$NON-NLS-1$
								if (subProgress != null) {
									subProgress.subTask(
										Util.bind("manager.filesToIndex", Integer.toString(awaitingWork))); //$NON-NLS-1$
									subProgress.worked(1);
								}
								previousJob = currentJob;
							}
							try {
								Thread.sleep(50);
							} catch (InterruptedException e) {
							}
						}
					} finally {
						synchronized(this) {
							this.awaitingClients--;
							if (this.thread != null) {
								this.thread.setPriority(originalPriority);
							}
						}
					}
					if (subProgress != null) {
						subProgress.done();
					}
			}
		}
		status = searchJob.execute(progress == null ? null : new SubProgressMonitor(progress, concurrentJobWork));
		if (progress != null) {
			progress.done();
		}
		if (VERBOSE)
			JobManager.verbose("DONE concurrent job - " + searchJob); //$NON-NLS-1$
		return status;
	}
	public abstract String processName();
	
	public synchronized void request(IJob job) {

		// append the job to the list of ones to process later on
		int size = awaitingJobs.length;
		if (++jobEnd == size) { // when growing, relocate jobs starting at position 0
			jobEnd -= jobStart;
			System.arraycopy(
				awaitingJobs,
				jobStart,
				(awaitingJobs = new IJob[size * 2]),
				0,
				jobEnd);
			jobStart = 0;
		}
		awaitingJobs[jobEnd] = job;
		if (VERBOSE)
			JobManager.verbose("REQUESTING job - " + job); //$NON-NLS-1$

	}
	/**
	 * Flush current state
	 */
	public void reset() {

		if (thread != null) {
			discardJobs(null); // discard all jobs
		} else {
			/* initiate background processing */
			thread = new Thread(this, this.processName());
			thread.setDaemon(true);
			// less prioritary by default, priority is raised if clients are actively waiting on it
			thread.setPriority(Thread.NORM_PRIORITY-1); 
			thread.start();
		}
	}
	/**
	 * Infinite loop performing resource indexing
	 */
	public void run() {

		long idlingStart = -1;
		activateProcessing();
		try {
			while (this.thread != null) {
				try {
					IJob job;
					if ((job = currentJob()) == null) {
						if (idlingStart < 0)
							idlingStart = System.currentTimeMillis();
						notifyIdle(System.currentTimeMillis() - idlingStart);
						Thread.currentThread().sleep(500);
						continue;
					} else {
						idlingStart = -1;
					}
					if (VERBOSE) {
						JobManager.verbose(awaitingJobsCount() + " awaiting jobs"); //$NON-NLS-1$
						JobManager.verbose("STARTING to execute - " + job); //$NON-NLS-1$
					}
					try {
						executing = true;
						/*boolean status = */job.execute(null);
						//if (status == FAILED) request(job);
					} finally {
						executing = false;
						if (VERBOSE) {
							JobManager.verbose("DONE executing - " + job); //$NON-NLS-1$
						}
						moveToNextJob();
						if (this.awaitingClients == 0) {
							Thread.sleep(50);
						}
					}
				} catch (InterruptedException e) { // background indexing was interrupted
				}
			}
		} catch (RuntimeException e) {
			if (this.thread != null) { // if not shutting down
				// log exception
				org.eclipse.jdt.internal.core.Util.log(e, "Background Indexer Crash Recovery"); //$NON-NLS-1$
				
				// keep job manager alive
				this.discardJobs(null);
				this.thread = null;
				this.reset(); // this will fork a new thread
				throw e;
			}
		}
	}
	/**
	 * Stop background processing, and wait until the current job is completed before returning
	 */
	public void shutdown() {

		disable();
		discardJobs(null); // will wait until current executing job has completed
		Thread thread = this.thread;
		this.thread = null; // mark the job manager as shutting down so that the thread will stop by itself
		try {
			thread.join();
		} catch (InterruptedException e) {
		}
	}
}