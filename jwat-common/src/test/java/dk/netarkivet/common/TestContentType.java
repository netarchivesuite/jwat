package dk.netarkivet.common;

import java.io.IOException;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class TestContentType {

	@Test
	public void test() throws IOException {
		ContentType ct;

		ct = ContentType.parseContentType(null);
		Assert.assertNull(ct);

		ct = ContentType.parseContentType("");
		Assert.assertNull(ct);

		ct = ContentType.parseContentType(" ");
		Assert.assertNull(ct);

		ct = ContentType.parseContentType("  ");
		Assert.assertNull(ct);

		ct = ContentType.parseContentType("text");
		Assert.assertNull(ct);

		ct = ContentType.parseContentType(" text");
		Assert.assertNull(ct);

		ct = ContentType.parseContentType("  text");
		Assert.assertNull(ct);

		ct = ContentType.parseContentType("text;");
		Assert.assertNull(ct);

		ct = ContentType.parseContentType(" text;");
		Assert.assertNull(ct);

		ct = ContentType.parseContentType("  text;");
		Assert.assertNull(ct);

		ct = ContentType.parseContentType("text/");
		Assert.assertNull(ct);

		ct = ContentType.parseContentType(" text/");
		Assert.assertNull(ct);

		ct = ContentType.parseContentType("  text/");
		Assert.assertNull(ct);

		ct = ContentType.parseContentType("text/;");
		Assert.assertNull(ct);

		ct = ContentType.parseContentType(" text/;");
		Assert.assertNull(ct);

		ct = ContentType.parseContentType("  text/;");
		Assert.assertNull(ct);
	}

}
