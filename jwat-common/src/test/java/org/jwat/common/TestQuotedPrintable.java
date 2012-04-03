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
package org.jwat.common;

import java.io.UnsupportedEncodingException;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class TestQuotedPrintable {

	@Test
	public void test_quotedprintable() {
		try {
			Assert.assertArrayEquals("this is some text".getBytes("ISO8859-1"), QuotedPrintable.decode("this=20is=20some=20text"));
			Assert.assertArrayEquals("Keith Moore".getBytes("ISO8859-1"), QuotedPrintable.decode("Keith_Moore"));
			Assert.assertArrayEquals("Keld Jørn Simonsen".getBytes("ISO8859-1"), QuotedPrintable.decode("Keld_J=F8rn_Simonsen"));
			Assert.assertArrayEquals("André".getBytes("ISO8859-1"), QuotedPrintable.decode("Andr=E9"));

			Assert.assertNull(QuotedPrintable.decode("Keld_J=FGrn_Simonsen"));
			Assert.assertNull(QuotedPrintable.decode("Keld_J=G8rn_Simonsen"));
			Assert.assertNull(QuotedPrintable.decode("Andr=E"));
			Assert.assertNull(QuotedPrintable.decode("\tAndré"));
			Assert.assertNull(QuotedPrintable.decode("\nAndré"));
			Assert.assertNull(QuotedPrintable.decode("André"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			Assert.fail("Unexpected exception!");
		}
	}
}
