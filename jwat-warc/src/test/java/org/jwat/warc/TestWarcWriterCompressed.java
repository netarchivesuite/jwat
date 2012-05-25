package org.jwat.warc;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class TestWarcWriterCompressed {

	@Test
	public void test_warcwriter_compressed() {
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();

			WarcWriterCompressed writer = (WarcWriterCompressed)WarcWriterFactory.getWriterCompressed(out);

			WarcRecord record = WarcRecord.createRecord(writer);
			record.header.addHeader("WARC-Type", "warcinfo");
			record.header.addHeader("WARC-Record-ID", "<urn:uuid:35f02b38-eb19-4f0d-86e4-bfe95815069c>");
			record.header.addHeader("WARC-Date", "2008-04-30T20:48:25Z");
			record.header.addHeader("WARC-Filename", "IAH-20080430204825-00000-blackbook.warc.gz");
			record.header.addHeader("Content-Length", "483");
			record.header.addHeader("Content-Type", "application/warc-fields");
		
			byte[] bytes = "Wecome to dænemark!".getBytes("UTF-8");

			ByteArrayInputStream in = new ByteArrayInputStream(bytes);

			writer.writeHeader(record);
			writer.streamPayload(in, bytes.length);
			writer.closeRecord();
			writer.close();

			out.close();

			String tmpStr = new String(out.toByteArray());
			System.out.println(tmpStr);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void test_warcwriter() {
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();

			WarcWriter writer = WarcWriterFactory.getWriter(out, false);

			WarcRecord record = WarcRecord.createRecord(writer);
			record.header.addHeader("WARC-Type", "warcinfo");
			record.header.addHeader("WARC-Record-ID", "<urn:uuid:35f02b38-eb19-4f0d-86e4-bfe95815069c>");
			record.header.addHeader("WARC-Date", "2008-04-30T20:48:25Z");
			record.header.addHeader("WARC-Filename", "IAH-20080430204825-00000-blackbook.warc.gz");
			record.header.addHeader("Content-Length", "483");
			record.header.addHeader("Content-Type", "application/warc-fields");

			byte[] bytes = "Wecome to dænemark!".getBytes("UTF-8");

			ByteArrayInputStream in = new ByteArrayInputStream(bytes);

			writer.writeHeader(record);
			writer.streamPayload(in, bytes.length);
			writer.closeRecord();
			writer.close();

			out.close();

			String tmpStr = new String(out.toByteArray());
			System.out.print(tmpStr);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
