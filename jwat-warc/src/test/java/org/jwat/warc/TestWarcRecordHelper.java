package org.jwat.warc;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

import org.junit.Assert;
import org.jwat.common.Diagnosis;

public class TestWarcRecordHelper {

	public void compareDiagnoses(Object[][] expectedDiagnoses, List<Diagnosis> diagnosisList) {
		Assert.assertEquals(expectedDiagnoses.length, diagnosisList.size());
		Diagnosis diagnosis;
		for (int i=0; i<expectedDiagnoses.length; ++i) {
			diagnosis = diagnosisList.get(i);
			Assert.assertEquals(expectedDiagnoses[i][0], diagnosis.type);
			Assert.assertEquals(expectedDiagnoses[i][1], diagnosis.entity);
			Assert.assertEquals(expectedDiagnoses[i][2], diagnosis.information.length);
		}
	}

	public WarcRecord createRecord(WarcWriter writer, Object[][] warcHeaders, WarcDigest blockDigest, WarcDigest payloadDigest) {
    	WarcRecord record = WarcRecord.createRecord(writer);
    	for (int i=0; i<warcHeaders.length; ++i) {
    		String fieldName = (String)warcHeaders[i][0];
    		String fieldValue = (String)warcHeaders[i][1];
    		record.header.addHeader(fieldName, fieldValue);
    	}
    	record.header.warcBlockDigest = blockDigest;
    	record.header.warcPayloadDigest = payloadDigest;
    	return record;
	}

	public long writeRecord(WarcWriter writer, WarcRecord record, byte[] httpHeaderBytes, byte[] payloadBytes) {
    	long written = 0;
		try {
			writer.writeHeader(record);
			if (httpHeaderBytes != null) {
				written += writer.streamPayload(new ByteArrayInputStream(httpHeaderBytes), 0);
			}
			if (payloadBytes != null) {
				written += writer.streamPayload(new ByteArrayInputStream(payloadBytes), 0);
			}
			writer.closeRecord();
		} catch (IOException e) {
			e.printStackTrace();
			Assert.fail("Unexepected exception!");
		}
		return written;
	}

	public WarcDigest createWarcDigest(Object[] digestParams) {
		WarcDigest warcDigest = null;
		if (digestParams != null) {
			String algorithm = (String)digestParams[0];
			byte[] digest = (byte[])digestParams[1];
			String encoding = (String)digestParams[2];
			String digestString = (String)digestParams[3];
        	warcDigest = WarcDigest.createWarcDigest(algorithm, digest, encoding, digestString);
		}
		return warcDigest;
	}

	public void writeRecords(WarcWriter writer, Object[][] warcHeaders, Object[][] writedata) {
		byte[] httpHeaderBytes = null;
		byte[] payloadBytes = null;
		Object[] blockDigestParams;
		Object[] payloadDigestParams;
    	WarcDigest blockDigest;
    	WarcDigest payloadDigest;
    	WarcRecord record;
    	for (int i=0; i<writedata.length; ++i) {
    		httpHeaderBytes = (byte[])writedata[i][0];
    		payloadBytes = (byte[])writedata[i][1];
    		blockDigestParams = (Object[])writedata[i][2];
    		payloadDigestParams = (Object[])writedata[i][3];
        	blockDigest = createWarcDigest(blockDigestParams);
        	payloadDigest = createWarcDigest(payloadDigestParams);
        	record = createRecord(writer, warcHeaders, blockDigest, payloadDigest);
        	writeRecord(writer, record, httpHeaderBytes, payloadBytes);
    	}
	}

}
