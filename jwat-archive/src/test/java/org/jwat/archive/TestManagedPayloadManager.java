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

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class TestManagedPayloadManager {

    @Test
    public void test_managedpayloadmanager() {
        ManagedPayloadManager mpm = ManagedPayloadManager.getInstance();
        Assert.assertNotNull(mpm);
        Assert.assertEquals(ManagedPayloadManager.DEFAULT_COPY_BUFFER_SIZE, mpm.copyBufferSize);
        Assert.assertEquals(ManagedPayloadManager.DEFAULT_IN_MEMORY_BUFFER_SIZE, mpm.inMemorybufferSize);
        Assert.assertEquals(0, mpm.managedPayloadQueue.size());

        ManagedPayload payload1a = mpm.checkout();
        Assert.assertEquals(0, mpm.managedPayloadQueue.size());
        mpm.checkin(payload1a);
        Assert.assertEquals(1, mpm.managedPayloadQueue.size());
        ManagedPayload payload1b = mpm.checkout();
        Assert.assertEquals(payload1a, payload1b);
        Assert.assertEquals(0, mpm.managedPayloadQueue.size());

        ManagedPayload payload2a = mpm.checkout();
        Assert.assertEquals(0, mpm.managedPayloadQueue.size());
        mpm.checkin(payload2a);
        Assert.assertEquals(1, mpm.managedPayloadQueue.size());
        ManagedPayload payload2b = mpm.checkout();
        Assert.assertEquals(payload2a, payload2b);
        Assert.assertEquals(0, mpm.managedPayloadQueue.size());

        Assert.assertNotEquals(payload1a, payload2a);
        Assert.assertNotEquals(payload1b, payload2b);

        mpm.checkin(payload2a);
        Assert.assertEquals(1, mpm.managedPayloadQueue.size());
        mpm.checkin(payload1a);
        Assert.assertEquals(2, mpm.managedPayloadQueue.size());

        ManagedPayload payload2c = mpm.checkout();
        Assert.assertEquals(1, mpm.managedPayloadQueue.size());
        ManagedPayload payload1c = mpm.checkout();
        Assert.assertEquals(0, mpm.managedPayloadQueue.size());

        Assert.assertEquals(payload1a, payload1c);
        Assert.assertEquals(payload2a, payload2c);
        Assert.assertNotEquals(payload1c, payload2c);

        mpm.checkin(payload2a);
        Assert.assertEquals(1, mpm.managedPayloadQueue.size());
        mpm.checkin(payload1a);
        Assert.assertEquals(2, mpm.managedPayloadQueue.size());

        Assert.assertTrue(payload1a.lock.tryAcquire());

        ManagedPayload payload2d = mpm.checkout();
        Assert.assertEquals(1, mpm.managedPayloadQueue.size());
        Assert.assertEquals(payload2a, payload2d);
        try {
            ManagedPayload payload1d = mpm.checkout();
            Assert.assertEquals(0, mpm.managedPayloadQueue.size());
            Assert.assertEquals(payload1a, payload1d);
            Assert.assertNotEquals(payload1d, payload2d);
            Assert.fail("Exception expected!");
        } catch (IllegalStateException e) {
        }

        Assert.assertEquals(ManagedPayloadManager.DEFAULT_COPY_BUFFER_SIZE, payload1a.copyBuf.length);
        Assert.assertEquals(ManagedPayloadManager.DEFAULT_IN_MEMORY_BUFFER_SIZE, payload1a.baios.getLength());
        Assert.assertEquals(ManagedPayloadManager.DEFAULT_COPY_BUFFER_SIZE, payload2a.copyBuf.length);
        Assert.assertEquals(ManagedPayloadManager.DEFAULT_IN_MEMORY_BUFFER_SIZE, payload2a.baios.getLength());

        ManagedPayloadManager mpm2 = ManagedPayloadManager.getInstance(1024, 16384);
        Assert.assertNotNull(mpm2);
        Assert.assertEquals(1024, mpm2.copyBufferSize);
        Assert.assertEquals(16384, mpm2.inMemorybufferSize);
        Assert.assertEquals(0, mpm2.managedPayloadQueue.size());

        ManagedPayload payload3a = mpm2.checkout();
        ManagedPayload payload3b = mpm2.checkout();
        Assert.assertNotEquals(payload3a, payload3b);

        Assert.assertEquals(1024, payload3a.copyBuf.length);
        Assert.assertEquals(16384, payload3a.baios.getLength());
        Assert.assertEquals(1024, payload3b.copyBuf.length);
        Assert.assertEquals(16384, payload3b.baios.getLength());
    }

}
