package dk.netarkivet.warclib;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import dk.netarkivet.gzip.GzipInputStream;

@RunWith(JUnit4.class)
public class TestParams {

	@Test
	public void test() throws IOException {
		String str;
		InputStream is;

		/*
		 * Digest.
		 */

		WarcDigest digest;

		digest = WarcDigest.parseDigest(null);
		Assert.assertNull(digest);

		digest = WarcDigest.parseDigest("");
		Assert.assertNull(digest);

		digest = WarcDigest.parseDigest("fail");
		Assert.assertNull(digest);

		digest = WarcDigest.parseDigest(":");
		Assert.assertNull(digest);

		digest = WarcDigest.parseDigest("sha1:");
		Assert.assertNull(digest);

		digest = WarcDigest.parseDigest(":AB2CD3EF4GH5IJ6KL7MN8OPQ");
		Assert.assertNull(digest);

		digest = WarcDigest.parseDigest("sha1:AB2CD3EF4GH5IJ6KL7MN8OPQ");
		Assert.assertNotNull(digest);

		str = digest.toString();
		Assert.assertNotNull(str);

		/*
		 * Date.
		 */

		Date warcDate;

		warcDate = WarcDateParser.getDate(null);
		Assert.assertNull(warcDate);

		warcDate = WarcDateParser.getDate("");
		Assert.assertNull(warcDate);

		warcDate = WarcDateParser.getDate("fail");
		Assert.assertNull(warcDate);

		warcDate = WarcDateParser.getDate("YYYY-MM-DDThh:mm:ssZ");
		Assert.assertNull(warcDate);

		warcDate = WarcDateParser.getDate("2011-12-24T19:30:00Z");
		Assert.assertNotNull(warcDate);

		/*
		 * WarcErrorType.
		 */

		WarcErrorType wet;

		wet = WarcErrorType.EMPTY;
		str = wet.toString();
		Assert.assertNotNull(str);

		wet = WarcErrorType.INVALID;
		str = wet.toString();
		Assert.assertNotNull(str);

		wet = WarcErrorType.DUPLICATE;
		str = wet.toString();
		Assert.assertNotNull(str);

		wet = WarcErrorType.UNKNOWN;
		str = wet.toString();
		Assert.assertNotNull(str);

		wet = WarcErrorType.WANTED;
		str = wet.toString();
		Assert.assertNotNull(str);

		wet = WarcErrorType.UNWANTED;
		str = wet.toString();
		Assert.assertNotNull(str);

		wet = WarcErrorType.RECOMMENDED;
		str = wet.toString();
		Assert.assertNotNull(str);

		/*
		 * WarcValidationError.
		 */

		WarcValidationError wve;

		try {
			new WarcValidationError(null, null, null);
			Assert.fail("Exception expected!");
		}
		catch (IllegalArgumentException e) {
		}

		try {
			wve = new WarcValidationError(WarcErrorType.INVALID, null, null);
			Assert.fail("Exception expected!");
		}
		catch (IllegalArgumentException e) {
		}

		wve = new WarcValidationError(WarcErrorType.WANTED, "help", null);
		Assert.assertNotNull(wve);
		str = wve.toString();
		Assert.assertNotNull(str);

		wve = new WarcValidationError(WarcErrorType.WANTED, "help", "me");
		Assert.assertNotNull(wve);
		str = wve.toString();
		Assert.assertNotNull(str);

		/*
		 * WarcReaderUncompressed.
		 */

		WarcReaderUncompressed readerUncompressed;

		readerUncompressed = new WarcReaderUncompressed();
		Assert.assertFalse(readerUncompressed.isCompressed());

		Assert.assertFalse(readerUncompressed.digest());
		readerUncompressed.setDigest(true);
		Assert.assertTrue(readerUncompressed.digest());
		readerUncompressed.setDigest(false);
		Assert.assertFalse(readerUncompressed.digest());

		readerUncompressed = new WarcReaderUncompressed();
		try {
			readerUncompressed = new WarcReaderUncompressed(null);
		}
		catch (IllegalArgumentException e) {
			readerUncompressed = null;
		}
		Assert.assertNull(readerUncompressed);

		readerUncompressed = new WarcReaderUncompressed();
		try {
			readerUncompressed.getNextRecord();
			Assert.fail("Exception expected!");
		}
		catch (IllegalStateException e) {
		}

		try {
			readerUncompressed.getNextRecordFrom(null);
			Assert.fail("Exception expected!");
		}
		catch (IllegalArgumentException e) {
		}

		try {
			readerUncompressed.getNextRecordFrom(null, 42);
			Assert.fail("Exception expected!");
		}
		catch (IllegalArgumentException e) {
		}

		is = new ByteArrayInputStream(new byte[] {42});

		try {
			readerUncompressed.getNextRecordFrom(is, -1);
			Assert.fail("Exception expected!");
		}
		catch (IllegalArgumentException e) {
		}

		try {
			readerUncompressed.getNextRecordFrom(is, 0);
			Assert.fail("Exception expected!");
		}
		catch (IllegalArgumentException e) {
		}

		is.close();
		is = null;

		/*
		 * WarcReaderCompressed.
		 */

		WarcReaderCompressed readerCompressed;

		readerCompressed = new WarcReaderCompressed();
		Assert.assertTrue(readerCompressed.isCompressed());

		Assert.assertFalse(readerCompressed.digest());
		readerCompressed.setDigest(true);
		Assert.assertTrue(readerCompressed.digest());
		readerCompressed.setDigest(false);
		Assert.assertFalse(readerCompressed.digest());

		readerCompressed = new WarcReaderCompressed();
		try {
			readerCompressed = new WarcReaderCompressed(null);
		}
		catch (IllegalArgumentException e) {
			readerCompressed = null;
		}
		Assert.assertNull(readerCompressed);

		readerCompressed = new WarcReaderCompressed();
		try {
			readerCompressed = new WarcReaderCompressed(null, 42);
		}
		catch (IllegalArgumentException e) {
			readerCompressed = null;
		}
		Assert.assertNull(readerCompressed);

		GzipInputStream gzis = new GzipInputStream(new ByteArrayInputStream(new byte[] {42}));

		readerCompressed = new WarcReaderCompressed();
		try {
			readerCompressed = new WarcReaderCompressed(gzis, -1);
		}
		catch (IllegalArgumentException e) {
			readerCompressed = null;
		}
		Assert.assertNull(readerCompressed);

		readerCompressed = new WarcReaderCompressed();
		try {
			readerCompressed = new WarcReaderCompressed(gzis, 0);
		}
		catch (IllegalArgumentException e) {
			readerCompressed = null;
		}
		Assert.assertNull(readerCompressed);

		gzis.close();
		gzis = null;

		readerCompressed = new WarcReaderCompressed();
		try {
			readerCompressed.getNextRecord();
			Assert.fail("Exception expected!");
		}
		catch (IllegalStateException e) {
		}

		try {
			readerCompressed.getNextRecordFrom(null);
			Assert.fail("Exception expected!");
		}
		catch (IllegalArgumentException e) {
		}

		try {
			readerCompressed.getNextRecordFrom(null, 42);
			Assert.fail("Exception expected!");
		}
		catch (IllegalArgumentException e) {
		}

		is = new ByteArrayInputStream(new byte[] {42});

		try {
			readerCompressed.getNextRecordFrom(is, -1);
			Assert.fail("Exception expected!");
		}
		catch (IllegalArgumentException e) {
		}

		try {
			readerCompressed.getNextRecordFrom(is, 0);
			Assert.fail("Exception expected!");
		}
		catch (IllegalArgumentException e) {
		}

		is.close();
		is = null;

		/*
		 * WarcReaderFactory.
		 */

		is = new ByteArrayInputStream(new byte[] {42});

		try {
			WarcReaderFactory.getReader(null, 42);
			Assert.fail("Exception expected!");
		}
		catch (IllegalArgumentException e) {
		}

		try {
			WarcReaderFactory.getReader(is, -1);
			Assert.fail("Exception expected!");
		}
		catch (IllegalArgumentException e) {
		}

		try {
			WarcReaderFactory.getReader(is, 0);
			Assert.fail("Exception expected!");
		}
		catch (IllegalArgumentException e) {
		}

		try {
			WarcReaderFactory.getReader(null);
			Assert.fail("Exception expected!");
		}
		catch (IllegalArgumentException e) {
		}

		try {
			WarcReaderFactory.getReaderUncompressed(null, 42);
			Assert.fail("Exception expected!");
		}
		catch (IllegalArgumentException e) {
		}

		try {
			WarcReaderFactory.getReaderUncompressed(is, -1);
			Assert.fail("Exception expected!");
		}
		catch (IllegalArgumentException e) {
		}

		try {
			WarcReaderFactory.getReaderUncompressed(is, 0);
			Assert.fail("Exception expected!");
		}
		catch (IllegalArgumentException e) {
		}

		try {
			WarcReaderFactory.getReaderUncompressed(null);
			Assert.fail("Exception expected!");
		}
		catch (IllegalArgumentException e) {
		}

		try {
			WarcReaderFactory.getReaderCompressed(null, 42);
			Assert.fail("Exception expected!");
		}
		catch (IllegalArgumentException e) {
		}

		try {
			WarcReaderFactory.getReaderCompressed(is, -1);
			Assert.fail("Exception expected!");
		}
		catch (IllegalArgumentException e) {
		}

		try {
			WarcReaderFactory.getReaderCompressed(is, 0);
			Assert.fail("Exception expected!");
		}
		catch (IllegalArgumentException e) {
		}

		try {
			WarcReaderFactory.getReaderCompressed(null);
			Assert.fail("Exception expected!");
		}
		catch (IllegalArgumentException e) {
		}

		is.close();
		is = null;
	}

}
