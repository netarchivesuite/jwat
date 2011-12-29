package org.jwat.arc;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.jwat.gzip.GzipInputStream;

@RunWith(JUnit4.class)
public class TestParams {

	@Test
	public void test() throws IOException {
		String str;
		InputStream is;

		/*
		 * Date.
		 */

		Date warcDate;

		warcDate = ArcDateParser.getDate(null);
		Assert.assertNull(warcDate);

		warcDate = ArcDateParser.getDate("");
		Assert.assertNull(warcDate);

		warcDate = ArcDateParser.getDate("fail");
		Assert.assertNull(warcDate);

		warcDate = ArcDateParser.getDate("yyyyMMddHHmmss");
		Assert.assertNull(warcDate);

		warcDate = ArcDateParser.getDate("20111224193000");
		Assert.assertNotNull(warcDate);

		/*
		 * WarcErrorType.
		 */

		/*
		 * ArcVersion.
		 */

		ArcVersion av;

		av = ArcVersion.VERSION_1;
		Assert.assertNotNull(av);
		str = av.toString();
		Assert.assertNotNull(str);

		av = ArcVersion.VERSION_1_1;
		Assert.assertNotNull(av);
		str = av.toString();
		Assert.assertNotNull(str);

		av = ArcVersion.VERSION_2;
		Assert.assertNotNull(av);
		str = av.toString();
		Assert.assertNotNull(str);

		/*
		 * ArcErrorType.
		 */

		ArcErrorType aet;

		aet = ArcErrorType.INVALID;
		Assert.assertNotNull(aet);
		str = aet.toString();
		Assert.assertNotNull(str);

		aet = ArcErrorType.MISSING;
		Assert.assertNotNull(aet);
		str = aet.toString();
		Assert.assertNotNull(str);

		/*
		 * ArcValidationError.
		 */

		ArcValidationError wve;

		try {
			new ArcValidationError(null, null, null);
			Assert.fail("Exception expected!");
		}
		catch (IllegalArgumentException e) {
		}

		try {
			wve = new ArcValidationError(ArcErrorType.INVALID, null, null);
			Assert.fail("Exception expected!");
		}
		catch (IllegalArgumentException e) {
		}

		wve = new ArcValidationError(ArcErrorType.MISSING, "help", null);
		Assert.assertNotNull(wve);
		str = wve.toString();
		Assert.assertNotNull(str);

		wve = new ArcValidationError(ArcErrorType.MISSING, "help", "me");
		Assert.assertNotNull(wve);
		str = wve.toString();
		Assert.assertNotNull(str);

		/*
		 * WarcReaderUncompressed.
		 */

		ArcReaderUncompressed readerUncompressed;

		readerUncompressed = new ArcReaderUncompressed();
		Assert.assertFalse(readerUncompressed.isCompressed());

		Assert.assertFalse(readerUncompressed.getBlockDigestEnabled());
		readerUncompressed.setBlockDigestEnabled(true);
		Assert.assertTrue(readerUncompressed.getBlockDigestEnabled());
		readerUncompressed.setBlockDigestEnabled(false);
		Assert.assertFalse(readerUncompressed.getBlockDigestEnabled());

		Assert.assertFalse(readerUncompressed.getPayloadDigestEnabled());
		readerUncompressed.setPayloadDigestEnabled(true);
		Assert.assertTrue(readerUncompressed.getPayloadDigestEnabled());
		readerUncompressed.setPayloadDigestEnabled(false);
		Assert.assertFalse(readerUncompressed.getPayloadDigestEnabled());

		try {
			Assert.assertNull(readerUncompressed.getBlockDigestAlgorithm());
			readerUncompressed.setBlockDigestAlgorithm("sha1");
			Assert.assertNotNull(readerUncompressed.getBlockDigestAlgorithm());
			readerUncompressed.setBlockDigestAlgorithm(null);
			Assert.assertNull(readerUncompressed.getBlockDigestAlgorithm());

			Assert.assertNull(readerUncompressed.getPayloadDigestAlgorithm());
			readerUncompressed.setPayloadDigestAlgorithm("sha1");
			Assert.assertNotNull(readerUncompressed.getPayloadDigestAlgorithm());
			readerUncompressed.setPayloadDigestAlgorithm(null);
			Assert.assertNull(readerUncompressed.getPayloadDigestAlgorithm());
		}
		catch (NoSuchAlgorithmException e1) {
			Assert.fail("Unexpected exception!");
		}

		readerUncompressed = new ArcReaderUncompressed();
		try {
			readerUncompressed.getVersionBlock(null);
			Assert.fail("Exception expected!");
		}
		catch (IllegalArgumentException e) {
		}

		readerUncompressed = new ArcReaderUncompressed();
		try {
			readerUncompressed.getVersionBlock();
			Assert.fail("Exception expected!");
		}
		catch (IllegalStateException e) {
		}

		readerUncompressed = new ArcReaderUncompressed();
		try {
			readerUncompressed = new ArcReaderUncompressed(null);
		}
		catch (IllegalArgumentException e) {
			readerUncompressed = null;
		}
		Assert.assertNull(readerUncompressed);

		readerUncompressed = new ArcReaderUncompressed();
		try {
			readerUncompressed.getNextRecord();
			Assert.fail("Exception expected!");
		}
		catch (IllegalStateException e) {
		}

		is = new ByteArrayInputStream(new byte[] {42});

		try {
			readerUncompressed.getNextRecordFrom(null, 0);
			Assert.fail("Exception expected!");
		}
		catch (IllegalArgumentException e) {
		}

		try {
			readerUncompressed.getNextRecordFrom(is, -1);
			Assert.fail("Exception expected!");
		}
		catch (IllegalArgumentException e) {
		}

		try {
			readerUncompressed.getNextRecordFrom(null, -1, 0);
			Assert.fail("Exception expected!");
		}
		catch (IllegalArgumentException e) {
		}

		try {
			readerUncompressed.getNextRecordFrom(is, 0, 0);
			Assert.fail("Exception expected!");
		}
		catch (IllegalArgumentException e) {
		}

		try {
			readerUncompressed.getNextRecordFrom(is, 42, -1);
			Assert.fail("Exception expected!");
		}
		catch (IllegalArgumentException e) {
		}

		is.close();
		is = null;

		/*
		 * ArcReaderCompressed.
		 */

		ArcReaderCompressed readerCompressed;

		readerCompressed = new ArcReaderCompressed();
		Assert.assertTrue(readerCompressed.isCompressed());

		Assert.assertFalse(readerCompressed.getBlockDigestEnabled());
		readerCompressed.setBlockDigestEnabled(true);
		Assert.assertTrue(readerCompressed.getBlockDigestEnabled());
		readerCompressed.setBlockDigestEnabled(false);
		Assert.assertFalse(readerCompressed.getBlockDigestEnabled());

		Assert.assertFalse(readerCompressed.getPayloadDigestEnabled());
		readerCompressed.setPayloadDigestEnabled(true);
		Assert.assertTrue(readerCompressed.getPayloadDigestEnabled());
		readerCompressed.setPayloadDigestEnabled(false);
		Assert.assertFalse(readerCompressed.getPayloadDigestEnabled());

		try {
			Assert.assertNull(readerCompressed.getBlockDigestAlgorithm());
			readerCompressed.setBlockDigestAlgorithm("sha1");
			Assert.assertNotNull(readerCompressed.getBlockDigestAlgorithm());
			readerCompressed.setBlockDigestAlgorithm(null);
			Assert.assertNull(readerCompressed.getBlockDigestAlgorithm());

			Assert.assertNull(readerCompressed.getPayloadDigestAlgorithm());
			readerCompressed.setPayloadDigestAlgorithm("sha1");
			Assert.assertNotNull(readerCompressed.getPayloadDigestAlgorithm());
			readerCompressed.setPayloadDigestAlgorithm(null);
			Assert.assertNull(readerCompressed.getPayloadDigestAlgorithm());
		}
		catch (NoSuchAlgorithmException e1) {
			Assert.fail("Unexpected exception!");
		}

		readerCompressed = new ArcReaderCompressed();
		try {
			readerCompressed.getVersionBlock(null);
			Assert.fail("Exception expected!");
		}
		catch (IllegalArgumentException e) {
		}

		readerCompressed = new ArcReaderCompressed();
		try {
			readerCompressed.getVersionBlock();
			Assert.fail("Exception expected!");
		}
		catch (IllegalStateException e) {
		}

		readerCompressed = new ArcReaderCompressed();
		try {
			readerCompressed = new ArcReaderCompressed(null);
		}
		catch (IllegalArgumentException e) {
			readerCompressed = null;
		}
		Assert.assertNull(readerCompressed);

		readerCompressed = new ArcReaderCompressed();
		try {
			readerCompressed = new ArcReaderCompressed(null, 42);
		}
		catch (IllegalArgumentException e) {
			readerCompressed = null;
		}
		Assert.assertNull(readerCompressed);

		GzipInputStream gzis = new GzipInputStream(new ByteArrayInputStream(new byte[] {42}));

		readerCompressed = new ArcReaderCompressed();
		try {
			readerCompressed = new ArcReaderCompressed(gzis, -1);
		}
		catch (IllegalArgumentException e) {
			readerCompressed = null;
		}
		Assert.assertNull(readerCompressed);

		readerCompressed = new ArcReaderCompressed();
		try {
			readerCompressed = new ArcReaderCompressed(gzis, 0);
		}
		catch (IllegalArgumentException e) {
			readerCompressed = null;
		}
		Assert.assertNull(readerCompressed);

		gzis.close();
		gzis = null;

		readerCompressed = new ArcReaderCompressed();
		try {
			readerCompressed.getNextRecord();
			Assert.fail("Exception expected!");
		}
		catch (IllegalStateException e) {
		}

		is = new ByteArrayInputStream(new byte[] {42});

		try {
			readerCompressed.getNextRecordFrom(null, 0);
			Assert.fail("Exception expected!");
		}
		catch (IllegalArgumentException e) {
		}

		try {
			readerCompressed.getNextRecordFrom(is, -1);
			Assert.fail("Exception expected!");
		}
		catch (IllegalArgumentException e) {
		}

		try {
			readerCompressed.getNextRecordFrom(null, 42, 0);
			Assert.fail("Exception expected!");
		}
		catch (IllegalArgumentException e) {
		}

		try {
			readerCompressed.getNextRecordFrom(is, -1, 0);
			Assert.fail("Exception expected!");
		}
		catch (IllegalArgumentException e) {
		}

		try {
			readerCompressed.getNextRecordFrom(is, 0, 0);
			Assert.fail("Exception expected!");
		}
		catch (IllegalArgumentException e) {
		}

		try {
			readerCompressed.getNextRecordFrom(is, 42, -1);
			Assert.fail("Exception expected!");
		}
		catch (IllegalArgumentException e) {
		}

		is.close();
		is = null;

		/*
		 * ArcReaderFactory.
		 */

		is = new ByteArrayInputStream(new byte[] {42});

		try {
			ArcReaderFactory.getReader(null, 42);
			Assert.fail("Exception expected!");
		}
		catch (IllegalArgumentException e) {
		}

		try {
			ArcReaderFactory.getReader(is, -1);
			Assert.fail("Exception expected!");
		}
		catch (IllegalArgumentException e) {
		}

		try {
			ArcReaderFactory.getReader(is, 0);
			Assert.fail("Exception expected!");
		}
		catch (IllegalArgumentException e) {
		}

		try {
			ArcReaderFactory.getReader(null);
			Assert.fail("Exception expected!");
		}
		catch (IllegalArgumentException e) {
		}

		try {
			ArcReaderFactory.getReaderUncompressed(null, 42);
			Assert.fail("Exception expected!");
		}
		catch (IllegalArgumentException e) {
		}

		try {
			ArcReaderFactory.getReaderUncompressed(is, -1);
			Assert.fail("Exception expected!");
		}
		catch (IllegalArgumentException e) {
		}

		try {
			ArcReaderFactory.getReaderUncompressed(is, 0);
			Assert.fail("Exception expected!");
		}
		catch (IllegalArgumentException e) {
		}

		try {
			ArcReaderFactory.getReaderUncompressed(null);
			Assert.fail("Exception expected!");
		}
		catch (IllegalArgumentException e) {
		}

		try {
			ArcReaderFactory.getReaderCompressed(null, 42);
			Assert.fail("Exception expected!");
		}
		catch (IllegalArgumentException e) {
		}

		try {
			ArcReaderFactory.getReaderCompressed(is, -1);
			Assert.fail("Exception expected!");
		}
		catch (IllegalArgumentException e) {
		}

		try {
			ArcReaderFactory.getReaderCompressed(is, 0);
			Assert.fail("Exception expected!");
		}
		catch (IllegalArgumentException e) {
		}

		try {
			ArcReaderFactory.getReaderCompressed(null);
			Assert.fail("Exception expected!");
		}
		catch (IllegalArgumentException e) {
		}

		is.close();
		is = null;
	}

}
