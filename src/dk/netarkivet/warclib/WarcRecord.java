package dk.netarkivet.warclib;
import java.io.IOException;

import org.jhove2.module.format.arc.ByteCountingInputStream;

public class WarcRecord {

	boolean bMagic;
	boolean bVersion;

	int major = -1;
	int minor = -1;

	String warcType;

	Long contentLength;

	public static WarcRecord parseRecord(ByteCountingInputStream in) {
		WarcRecord wr = new WarcRecord();
		try {
			if (wr.checkMagicVersion(in)) {
				System.out.println(wr.bMagic);
				System.out.println(wr.bVersion);
				System.out.println(wr.major + "." + wr.minor);

				wr.parseFields(in);

				if (wr.warcType != null) {
					Integer rt_idx = WarcConstants.recordTypeIdxMap.get(wr.warcType);
					if (rt_idx != null) {
						System.out.println("WARC-Type-Idx: " + rt_idx.intValue());
					}
					System.out.println("WARC-Type: " + wr.warcType);
				}
				if (wr.contentLength != null) {
					System.out.println("Content-Length: " + wr.contentLength);

					in.skip(wr.contentLength);
				}
			}
			else {
				wr = null;
			}
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return wr;
	}

	public boolean checkMagicVersion(ByteCountingInputStream in) throws IOException {
		bMagic = false;
		bVersion = false;
		String tmpStr;
		boolean bSeekMagic = true;
		while (bSeekMagic) {
			tmpStr = in.readLine();
			if (tmpStr != null) {
				if (tmpStr.length() > 0) {
					if (tmpStr.startsWith(WarcConstants.WARC_MAGIC_HEADER)) {
						bMagic = true;
						String versionStr = tmpStr.substring(WarcConstants.WARC_MAGIC_HEADER.length());
						String[] tmpArr = versionStr.split("\\.", -1);		// Slow?
						if (tmpArr.length >= 2 && tmpArr.length <= 4) {
							bVersion = true;
							int[] versionArr = new int[tmpArr.length];
							for (int i=0; i<tmpArr.length; ++i) {
								try {
									versionArr[i] = Integer.parseInt(tmpArr[i]);
								}
								catch (NumberFormatException e) {
									versionArr[i] = -1;
								}
							}
							major = versionArr[0];
							minor = versionArr[1];
						}
						bSeekMagic = false;
					}
					else {
						// Gibberish.
					}
				}
				else {
					// Empty line.
				}
			}
			else {
				// EOF.
				bSeekMagic = false;
			}
		}
		return bMagic;
	}

	public void parseFields(ByteCountingInputStream in) throws IOException {
		String tmpStr;
		boolean bFields = true;
		while (bFields) {
			tmpStr = in.readLine();
			if (tmpStr != null) {
				while (tmpStr.endsWith("\r")) {
					tmpStr = tmpStr.substring(0, tmpStr.length() - 1);
				}
				if ( tmpStr.length() > 0 ) {
					if (!Character.isWhitespace(tmpStr.charAt(0))) {
						int idx = tmpStr.indexOf(':');
						if (idx != -1) {
							String field = tmpStr.substring(0, idx);
							String value = tmpStr.substring(idx + 1).trim();

							Integer fn_idx = WarcConstants.fieldNameIdxMap.get(field.toUpperCase());
							if (fn_idx != null) {
								switch (fn_idx.intValue()) {
								case WarcConstants.FN_IDX_WARC_TYPE:
									warcType = value;
									break;
								case WarcConstants.FN_IDX_WARC_RECORD_ID:
									break;
								case WarcConstants.FN_IDX_WARC_DATE:
									break;
								case WarcConstants.FN_IDX_CONTENT_LENGTH:
									try {
										contentLength = Long.parseLong(value);
									}
									catch (NumberFormatException e) {
									}
									break;
								case WarcConstants.FN_IDX_CONTENT_TYPE:
									break;
								case WarcConstants.FN_IDX_WARC_CONCURRENT_TO:
									break;
								case WarcConstants.FN_IDX_WARC_BLOCK_DIGEST:
									break;
								case WarcConstants.FN_IDX_WARC_PAYLOAD_DIGEST:
									break;
								case WarcConstants.FN_IDX_WARC_IP_ADDRESS:
									break;
								case WarcConstants.FN_IDX_WARC_REFERS_TO:
									break;
								case WarcConstants.FN_IDX_WARC_TARGET_URI:
									break;
								case WarcConstants.FN_IDX_WARC_TRUNCATED:
									break;
								case WarcConstants.FN_IDX_WARC_WARCINFO_ID:
									break;
								case WarcConstants.FN_IDX_WARC_FILENAME:
									break;
								case WarcConstants.FN_IDX_WARC_PROFILE:
									break;
								case WarcConstants.FN_IDX_WARC_IDENTIFIED_PAYLOAD_TYPE:
									break;
								case WarcConstants.FN_IDX_WARC_SEGMENT_ORIGIN_ID:
									break;
								case WarcConstants.FN_IDX_WARC_SEGMENT_NUMBER:
									break;
								case WarcConstants.FN_IDX_WARC_SEGMENT_TOTAL_LENGTH:
									break;
								}
							}
							else {
								// Unrecognized field name.
							}
						}
					}
					else {
						// Leading Whitespace.
					}
				}
				else {
					// Empty line.
					bFields = false;
				}
			}
			else {
				// EOF.
				bFields = false;
			}
		}
	}

}
