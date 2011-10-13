package dk.netarkivet.warclib;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.jhove2.module.format.arc.ByteCountingInputStream;

public class WarcParser {

	protected ByteCountingInputStream in;

	public WarcParser(InputStream in) {
		this.in = new ByteCountingInputStream( in );
	}

	public void close() {
		if (in != null) {
			try {
				in.close();
			}
			catch (IOException e) { /* ignore */ }
			in = null;
		}
	}

	public WarcRecord nextRecord() {
		return WarcRecord.parseRecord( in );
	}

	public WarcRecord nextRecord(InputStream in) {
		return WarcRecord.parseRecord( new ByteCountingInputStream( in ) );
	}

	public Iterator<WarcRecord> iterator() {
		return new Iterator<WarcRecord>() {

			private WarcRecord next;

			private WarcRecord current;

			@Override
			public boolean hasNext() {
				if (next == null) {
					next = nextRecord();
				}
				return (next != null);
			}

			@Override
			public WarcRecord next() {
				if (next == null) {
					next = nextRecord();
				}
				if (next == null) {
					throw new NoSuchElementException();
				}
				current = next;
				next = null;
				return current;
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}

}
