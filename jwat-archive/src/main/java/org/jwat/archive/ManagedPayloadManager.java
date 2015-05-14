/**
 * Java Web Archive Toolkit - Software to read and validate ARC, WARC
 * and GZip files. (http://jwat.org/)
 * Copyright 2011-2012 Netarkivet.dk (http://netarkivet.dk/)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jwat.archive;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;

public class ManagedPayloadManager {

	public static final int DEFAULT_COPY_BUFFER_SIZE = 8192;

	public static final int DEFAULT_IN_MEMORY_BUFFER_SIZE = 10*1024*1024;

	public static ManagedPayloadManager getInstance() {
		return getInstance(DEFAULT_COPY_BUFFER_SIZE, DEFAULT_IN_MEMORY_BUFFER_SIZE);
	}

	public static ManagedPayloadManager getInstance(int copyBufferSize, int inMemorybufferSize) {
		ManagedPayloadManager mpm = new ManagedPayloadManager();
		mpm.copyBufferSize = copyBufferSize;
		mpm.inMemorybufferSize = inMemorybufferSize;
		return mpm;
	}

	protected Semaphore queueLock = new Semaphore(1);

	protected ConcurrentLinkedQueue<ManagedPayload> managedPayloadQueue = new ConcurrentLinkedQueue<ManagedPayload>();

	protected int copyBufferSize;

	protected int inMemorybufferSize;

	public ManagedPayload checkout() {
		ManagedPayload managedPayload = null;
		queueLock.acquireUninterruptibly();
		managedPayload = managedPayloadQueue.poll();
		if (managedPayload == null) {
			managedPayload = new ManagedPayload(copyBufferSize, inMemorybufferSize);
		}
		if (!managedPayload.lock.tryAcquire()) {
			throw new IllegalStateException();
		}
		queueLock.release();
		return managedPayload;
	}

	public void checkin(ManagedPayload managedPayload) {
		queueLock.acquireUninterruptibly();
		managedPayload.lock.release();
		managedPayloadQueue.add(managedPayload);
		queueLock.release();
	}

}
