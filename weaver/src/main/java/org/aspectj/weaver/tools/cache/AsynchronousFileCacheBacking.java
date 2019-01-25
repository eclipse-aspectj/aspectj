/*******************************************************************************
 * Copyright (c) 2012 Contributors.
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Lyor Goldstein (vmware)	add support for weaved class being re-defined
 *******************************************************************************/

package org.aspectj.weaver.tools.cache;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;

import org.aspectj.util.FileUtil;
import org.aspectj.util.LangUtil;
import org.aspectj.weaver.tools.Trace;
import org.aspectj.weaver.tools.TraceFactory;

/**
 * Uses a background thread to do the actual I/O and for caching &quot;persistence&quot;
 * so that the caching works faster on repeated activations of the application.
 * The class maintains an in-memory cache, and uses a queue of {@link AsyncCommand}s
 * to signal to a background thread various actions required to &quot;synchronize&quot;
 * the in-memory cache with the persisted copy. Whenever there is a cache miss
 * from the {@link #get(CachedClassReference)} call, the weaver issues a
 * {@link #put(CachedClassEntry)} call. This call has 2 side-effects:</BR>
 * <UL>
 * 		<LI>
 * 		The in-memory cache is updated so that subsequent calls to {@link #get(CachedClassReference)}
 * 		will not return the mapped value.
 * 		</LI>
 * 
 *  	<LI>
 *  	An &quot;update index&quot {@link AsyncCommand} is posted to the background
 *  	thread so that the newly mapped value will be persisted (eventually)
 *  	</LI> 
 * </UL>
 * The actual persistence is implemented by the <U>concrete</U> classes
 */
public abstract class AsynchronousFileCacheBacking extends AbstractIndexedFileCacheBacking {
    private static final BlockingQueue<AsyncCommand>   commandsQ=new LinkedBlockingQueue<AsyncCommand>();
    private static final ExecutorService    execService=Executors.newSingleThreadExecutor();
    private static Future<?> commandsRunner;

    protected final Map<String, IndexEntry> index, exposedIndex;
    protected final Map<String, byte[]>   bytesMap, exposedBytes;

    protected AsynchronousFileCacheBacking (File cacheDir) {
    	super(cacheDir);

        index = readIndex(cacheDir, getIndexFile());
        exposedIndex = Collections.unmodifiableMap(index);
        bytesMap = readClassBytes(index, cacheDir);
        exposedBytes = Collections.unmodifiableMap(bytesMap);
    }

    @Override
	protected Map<String, IndexEntry> getIndex() {
		return index;
	}

	public CachedClassEntry get(CachedClassReference ref, byte[] originalBytes) {
        String              key=ref.getKey();
        final IndexEntry    indexEntry;
        synchronized(index) {
            if ((indexEntry=index.get(key)) == null) {
                return null;
            }
        }

        if (crc(originalBytes) != indexEntry.crcClass) {
            if ((logger != null) && logger.isTraceEnabled()) {
                logger.debug("get(" + getCacheDirectory() + ") mismatched original class bytes CRC for " + key);
            }

            remove(key);
            return null;
        }

        if (indexEntry.ignored) {
            return new CachedClassEntry(ref, WeavedClassCache.ZERO_BYTES, CachedClassEntry.EntryType.IGNORED);
        }

        final byte[]    bytes;
        synchronized(bytesMap) {
            /*
             * NOTE: we assume that keys represent classes so if we have their
             * bytes they will not be re-created
             */
            if ((bytes=bytesMap.remove(key)) == null) {
                return null;
            }
        }

        if (indexEntry.generated) {
            return new CachedClassEntry(ref, bytes, CachedClassEntry.EntryType.GENERATED);
        } else {
            return new CachedClassEntry(ref, bytes, CachedClassEntry.EntryType.WEAVED);
        }
    }

    public void put(CachedClassEntry entry, byte[] originalBytes) {
        String  key=entry.getKey();
        byte[]  bytes=entry.isIgnored() ? null : entry.getBytes();
        synchronized(index) {
            IndexEntry  indexEntry=index.get(key);
            if (indexEntry != null) {
                return;
            }

            /*
             * Note: we do not cache the class bytes - only send them to
             * be saved. The assumption is that the 'put' call was invoked
             * because 'get' failed to return any bytes. And since we assume
             * that each class bytes are required only once, there is no
             * need to cache them
             */
            indexEntry = createIndexEntry(entry, originalBytes);
            index.put(key, indexEntry);
        }

        if (!postCacheCommand(new InsertCommand(this, key, bytes))) {
            if ((logger != null) && logger.isTraceEnabled()) {
                logger.error("put(" + getCacheDirectory() + ") Failed to post insert command for " + key);
            }
        }

        if ((logger != null) && logger.isTraceEnabled()) {
            logger.debug("put(" + getCacheDirectory() + ")[" + key + "] inserted");
        }
    }

    public void remove(CachedClassReference ref) {
    	remove(ref.getKey());
    }

    protected IndexEntry remove (String key) {
        IndexEntry  entry;
        synchronized(index) {
            entry = index.remove(key);
        }

        synchronized(bytesMap) {
            bytesMap.remove(key);
        }

        if (!postCacheCommand(new RemoveCommand(this, key))) {
            if ((logger != null) && logger.isTraceEnabled()) {
                logger.error("remove(" + getCacheDirectory() + ") Failed to post remove command for " + key);
            }
        }
        
        if (entry != null) {
            if (!key.equals(entry.key)) {
                if ((logger != null) && logger.isTraceEnabled()) {
                    logger.error("remove(" + getCacheDirectory() + ") Mismatched keys: " + key + " / " + entry.key);
                }
            } else if ((logger != null) && logger.isTraceEnabled()) {
                logger.debug("remove(" + getCacheDirectory() + ")[" + key + "] removed");
            }
        }

        return entry;
    }

    public List<IndexEntry> getIndexEntries () {
        synchronized(index) {
            if (index.isEmpty()) {
                return Collections.emptyList();
            } else {
                return new ArrayList<IndexEntry>(index.values());
            }
        }
    }

    public Map<String, IndexEntry> getIndexMap () {
        return exposedIndex;
    }

    public Map<String, byte[]> getBytesMap () {
        return exposedBytes;
    }

    public void clear() {
        synchronized(index) {
            index.clear();
        }

        if (!postCacheCommand(new ClearCommand(this))) {
            if ((logger != null) && logger.isTraceEnabled()) {
                logger.error("Failed to post clear command for " + getIndexFile());
            }
        }
    }

    protected void executeCommand (AsyncCommand cmd) throws Exception {
        if (cmd instanceof ClearCommand) {
            executeClearCommand();
        } else if (cmd instanceof UpdateIndexCommand) {
            executeUpdateIndexCommand();
        } else if (cmd instanceof InsertCommand) {
            executeInsertCommand((InsertCommand) cmd);
        } else if (cmd instanceof RemoveCommand) {
            executeRemoveCommand((RemoveCommand) cmd);
        } else {
            throw new UnsupportedOperationException("Unknown command: " + cmd);
        }
    }

    protected void executeClearCommand () throws Exception {
        FileUtil.deleteContents(getIndexFile());
        FileUtil.deleteContents(getCacheDirectory());
    }

    protected void executeUpdateIndexCommand () throws Exception {
        writeIndex(getIndexFile(), getIndexEntries());
    }

    protected void executeInsertCommand (InsertCommand cmd) throws Exception {
        writeIndex(getIndexFile(), getIndexEntries());

        byte[]  bytes=cmd.getClassBytes();
        if (bytes != null) {
            writeClassBytes(cmd.getKey(), bytes);
        }
    }

    protected void executeRemoveCommand (RemoveCommand cmd) throws Exception {
        Exception err=null;
        try {
            removeClassBytes(cmd.getKey());
        } catch(Exception e) {
            err = e;
        }

        writeIndex(getIndexFile(), getIndexEntries());

        if (err != null) {
            throw err;  // check if the class bytes remove had any problems
        }
    }

    /**
     * Helper for {@link #executeRemoveCommand(RemoveCommand)}
     * @param key The key representing the class whose bytes are to be removed
     * @throws Exception if failed to remove class bytes
     */
    protected abstract void removeClassBytes (String key) throws Exception;

    protected abstract Map<String, byte[]> readClassBytes (Map<String,IndexEntry> indexMap, File cacheDir);

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + String.valueOf(getCacheDirectory()) + "]";
    }

    protected static final <T extends AsynchronousFileCacheBacking> T createBacking (
                            File cacheDir, AsynchronousFileCacheBackingCreator<T> creator) {
        final Trace trace=TraceFactory.getTraceFactory().getTrace(AsynchronousFileCacheBacking.class);
        if (!cacheDir.exists()) {
            if (!cacheDir.mkdirs()) {
                if ((trace != null) && trace.isTraceEnabled()) {
                    trace.error("Unable to create cache directory at " + cacheDir.getAbsolutePath());
                }
                return null;
            }
        }

        if (!cacheDir.canWrite()) {
            if ((trace != null) && trace.isTraceEnabled()) {
                trace.error("Cache directory is not writable at " + cacheDir.getAbsolutePath());
            }
            return null;
        }

        // start the service (if needed) only if successfully create the backing instance
        T    backing=creator.create(cacheDir);
        synchronized(execService) {
            if (commandsRunner == null) {
                commandsRunner = execService.submit(new Runnable() {
                    @SuppressWarnings("synthetic-access")
                    public void run() {
                        for ( ; ; ) {
                            try {
                                AsyncCommand    cmd=commandsQ.take();
                                try {
                                    AsynchronousFileCacheBacking    cache=cmd.getCache();
                                    cache.executeCommand(cmd);
                                } catch(Exception e) {
                                    if ((trace != null) && trace.isTraceEnabled()) {
                                        trace.error("Failed (" + e.getClass().getSimpleName() + ")"
                                                  + " to execute " + cmd + ": " + e.getMessage(), e);
                                    }
                                }
                            } catch(InterruptedException e) {
                                if ((trace != null) && trace.isTraceEnabled()) {
                                    trace.warn("Interrupted");
                                }
                                Thread.currentThread().interrupt();
                                break;
                            }
                        }
                    }
                });
            }
        }

        // fire-up an update-index command in case index was changed by the constructor
        if (!postCacheCommand(new UpdateIndexCommand(backing))) {
            if ((trace != null) && trace.isTraceEnabled()) {
                trace.warn("Failed to offer update index command to " + cacheDir.getAbsolutePath());
            }
        }

        return backing;
    }

    public static final boolean postCacheCommand (AsyncCommand cmd) {
        return commandsQ.offer(cmd);
    }

    public static interface AsynchronousFileCacheBackingCreator<T extends AsynchronousFileCacheBacking> {
        T create (File cacheDir);
    }
    /**
     * Represents an asynchronous command that can be sent to the
     * {@link AsynchronousFileCacheBacking} instance to be executed
     * on it <U>asynchronously</U>
     */
    public static interface AsyncCommand {
        /**
         * @return The {@link AsynchronousFileCacheBacking} on which
         * this command is supposed to be executed
         * @see AsynchronousFileCacheBacking#executeCommand(AsyncCommand)
         */
        AsynchronousFileCacheBacking getCache ();
    }

    public static abstract class AbstractCommand implements AsyncCommand {
        private final AsynchronousFileCacheBacking  cache;
        protected AbstractCommand (AsynchronousFileCacheBacking backing) {
            if ((cache=backing) == null) {
                throw new IllegalStateException("No backing cache specified");
            }
        }

        public final AsynchronousFileCacheBacking getCache () {
            return cache;
        }
        
        @Override
        public String toString() {
            return getClass().getSimpleName() + "[" + getCache() + "]";
        }
    }

    public static class ClearCommand extends AbstractCommand {
        public ClearCommand (AsynchronousFileCacheBacking cache) {
            super(cache);
        }
    }

    public static class UpdateIndexCommand extends AbstractCommand {
        public UpdateIndexCommand (AsynchronousFileCacheBacking cache) {
            super(cache);
        }
    }

    /**
     * Base class for {@link AbstractCommand}s that refer to a cache key
     */
    public static abstract class KeyedCommand extends AbstractCommand {
        private final String    key;
        protected KeyedCommand (AsynchronousFileCacheBacking cache, String keyValue) {
            super(cache);
            
            if (LangUtil.isEmpty(keyValue)) {
                throw new IllegalStateException("No key value");
            }

            key = keyValue;
        }

        public final String getKey () {
            return key;
        }

        @Override
        public String toString() {
            return super.toString() + "[" + getKey() + "]";
        }
    }

    public static class RemoveCommand extends KeyedCommand {
        public RemoveCommand (AsynchronousFileCacheBacking cache, String keyValue) {
            super(cache, keyValue);
        }
    }
    
    public static class InsertCommand extends KeyedCommand {
        private final byte[]    bytes;

        public InsertCommand (AsynchronousFileCacheBacking cache, String keyValue, byte[] classBytes) {
            super(cache, keyValue);
            bytes = classBytes;
        }

        public final byte[] getClassBytes () {
            return bytes;
        }
    }
}
