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

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class TestArrayUtils {

	/**
	 * Test skipSpaces().
	 */
	@Test
	public void test_arrayutils_skip() {
		ArrayUtils arrayUtils = new ArrayUtils();
		Assert.assertNotNull(arrayUtils);

		byte[] arr;

		arr = "                ".getBytes();

		/*
		 * skip().
		 */

		Assert.assertEquals(16, ArrayUtils.skip(ArrayUtils.SKIP_WHITESPACE, arr, 0));
		Assert.assertEquals(16, ArrayUtils.skip(ArrayUtils.SKIP_WHITESPACE, arr, 8));
		Assert.assertEquals(16, ArrayUtils.skip(ArrayUtils.SKIP_WHITESPACE, arr, 16));
		Assert.assertEquals(17, ArrayUtils.skip(ArrayUtils.SKIP_WHITESPACE, arr, 17));

		arr = "0123456789abcdef".getBytes();
		Assert.assertEquals(0, ArrayUtils.skip(ArrayUtils.SKIP_WHITESPACE, arr, 0));
		Assert.assertEquals(8, ArrayUtils.skip(ArrayUtils.SKIP_WHITESPACE, arr, 8));
		Assert.assertEquals(16, ArrayUtils.skip(ArrayUtils.SKIP_WHITESPACE, arr, 16));
		Assert.assertEquals(17, ArrayUtils.skip(ArrayUtils.SKIP_WHITESPACE, arr, 17));

		Assert.assertEquals(9, ArrayUtils.skip(ArrayUtils.SKIP_NONWHITESPACE, ArrayUtils.CASE_SENSITIVE, 0));
		Assert.assertEquals(9, ArrayUtils.skip(ArrayUtils.SKIP_NONWHITESPACE, ArrayUtils.CASE_SENSITIVE, 9));
		Assert.assertEquals(10, ArrayUtils.skip(ArrayUtils.SKIP_NONWHITESPACE, ArrayUtils.CASE_SENSITIVE, 10));
		Assert.assertEquals(13, ArrayUtils.skip(ArrayUtils.SKIP_NONWHITESPACE, ArrayUtils.CASE_SENSITIVE, 11));
		Assert.assertEquals(13, ArrayUtils.skip(ArrayUtils.SKIP_NONWHITESPACE, ArrayUtils.CASE_SENSITIVE, 13));
		Assert.assertEquals(32, ArrayUtils.skip(ArrayUtils.SKIP_NONWHITESPACE, ArrayUtils.CASE_SENSITIVE, 14));
		Assert.assertEquals(32, ArrayUtils.skip(ArrayUtils.SKIP_NONWHITESPACE, ArrayUtils.CASE_SENSITIVE, 32));
		Assert.assertEquals(256, ArrayUtils.skip(ArrayUtils.SKIP_NONWHITESPACE, ArrayUtils.CASE_SENSITIVE, 33));
	}

	/**
	 * Test startsWith() and startsWithIgnoreCase().
	 */

	@Test
	public void test_arrayutils_startswith() {
		ArrayUtils arrayUtils = new ArrayUtils();
		Assert.assertNotNull(arrayUtils);

		byte[] arr;
		byte[] arr2;

		arr = "<HTML>".getBytes();
		arr2 = "<html>".getBytes();

		/*
		 * startsWith().
		 */

		Assert.assertTrue(ArrayUtils.startsWith("<HTML".getBytes(), arr));
		Assert.assertFalse(ArrayUtils.startsWith("<html".getBytes(), arr));
		Assert.assertTrue(ArrayUtils.startsWith("<HTML>".getBytes(), arr));
		Assert.assertFalse(ArrayUtils.startsWith("<html>".getBytes(), arr));
		Assert.assertFalse(ArrayUtils.startsWith("HTML>".getBytes(), arr));
		Assert.assertFalse(ArrayUtils.startsWith("html>".getBytes(), arr));
		Assert.assertFalse(ArrayUtils.startsWith("<HTML> ".getBytes(), arr));
		Assert.assertFalse(ArrayUtils.startsWith("<html> ".getBytes(), arr));

		Assert.assertFalse(ArrayUtils.startsWith("<HTML".getBytes(), arr2));
		Assert.assertTrue(ArrayUtils.startsWith("<html".getBytes(), arr2));
		Assert.assertFalse(ArrayUtils.startsWith("<HTML>".getBytes(), arr2));
		Assert.assertTrue(ArrayUtils.startsWith("<html>".getBytes(), arr2));
		Assert.assertFalse(ArrayUtils.startsWith("HTML>".getBytes(), arr2));
		Assert.assertFalse(ArrayUtils.startsWith("html>".getBytes(), arr2));
		Assert.assertFalse(ArrayUtils.startsWith("<HTML> ".getBytes(), arr2));
		Assert.assertFalse(ArrayUtils.startsWith("<html> ".getBytes(), arr2));

		Assert.assertFalse(ArrayUtils.startsWith("<HEAD>".getBytes(), arr));
		Assert.assertFalse(ArrayUtils.startsWith("<head>".getBytes(), arr));
		Assert.assertFalse(ArrayUtils.startsWith("<HEAD>".getBytes(), arr2));
		Assert.assertFalse(ArrayUtils.startsWith("<head>".getBytes(), arr2));

		/*
		 * startsWithIgnoreCase().
		 */

		Assert.assertTrue(ArrayUtils.startsWithIgnoreCase("<HTML".getBytes(), arr));
		Assert.assertTrue(ArrayUtils.startsWithIgnoreCase("<html".getBytes(), arr));
		Assert.assertTrue(ArrayUtils.startsWithIgnoreCase("<HTML>".getBytes(), arr));
		Assert.assertTrue(ArrayUtils.startsWithIgnoreCase("<html>".getBytes(), arr));
		Assert.assertFalse(ArrayUtils.startsWithIgnoreCase("HTML>".getBytes(), arr));
		Assert.assertFalse(ArrayUtils.startsWithIgnoreCase("html>".getBytes(), arr));
		Assert.assertFalse(ArrayUtils.startsWithIgnoreCase("<HTML> ".getBytes(), arr));
		Assert.assertFalse(ArrayUtils.startsWithIgnoreCase("<html> ".getBytes(), arr));

		Assert.assertTrue(ArrayUtils.startsWithIgnoreCase("<HTML".getBytes(), arr2));
		Assert.assertTrue(ArrayUtils.startsWithIgnoreCase("<html".getBytes(), arr2));
		Assert.assertTrue(ArrayUtils.startsWithIgnoreCase("<HTML>".getBytes(), arr2));
		Assert.assertTrue(ArrayUtils.startsWithIgnoreCase("<html>".getBytes(), arr2));
		Assert.assertFalse(ArrayUtils.startsWithIgnoreCase("HTML>".getBytes(), arr2));
		Assert.assertFalse(ArrayUtils.startsWithIgnoreCase("html>".getBytes(), arr2));
		Assert.assertFalse(ArrayUtils.startsWithIgnoreCase("<HTML> ".getBytes(), arr2));
		Assert.assertFalse(ArrayUtils.startsWithIgnoreCase("<html> ".getBytes(), arr2));

		Assert.assertFalse(ArrayUtils.startsWithIgnoreCase("<HEAD>".getBytes(), arr));
		Assert.assertFalse(ArrayUtils.startsWithIgnoreCase("<head>".getBytes(), arr));
		Assert.assertFalse(ArrayUtils.startsWithIgnoreCase("<HEAD>".getBytes(), arr2));
		Assert.assertFalse(ArrayUtils.startsWithIgnoreCase("<head>".getBytes(), arr2));
	}

	/**
	 * Test equalsAt() and equalsAtIgnoreCase.
	 */
	@Test
	public void test_arrayutils_equalsat() {
		ArrayUtils arrayUtils = new ArrayUtils();
		Assert.assertNotNull(arrayUtils);

		byte[] arr;
		byte[] arr2;

		arr = " <HTML> ".getBytes();
		arr2 = " <html> ".getBytes();

		/*
		 * equalsAt().
		 */

		Assert.assertFalse(ArrayUtils.equalsAt("<HTML>".getBytes(), arr, 0));
		Assert.assertTrue(ArrayUtils.equalsAt("<HTML>".getBytes(), arr, 1));
		Assert.assertFalse(ArrayUtils.equalsAt("<HTML>".getBytes(), arr, 2));
		Assert.assertFalse(ArrayUtils.equalsAt("<HTML>".getBytes(), arr, 5));
		Assert.assertFalse(ArrayUtils.equalsAt("<HTML>".getBytes(), arr, 10));

		Assert.assertFalse(ArrayUtils.equalsAt("<html>".getBytes(), arr, 0));
		Assert.assertFalse(ArrayUtils.equalsAt("<html>".getBytes(), arr, 1));
		Assert.assertFalse(ArrayUtils.equalsAt("<html>".getBytes(), arr, 2));
		Assert.assertFalse(ArrayUtils.equalsAt("<html>".getBytes(), arr, 5));
		Assert.assertFalse(ArrayUtils.equalsAt("<html>".getBytes(), arr, 10));

		Assert.assertFalse(ArrayUtils.equalsAt("<HTML>".getBytes(), arr2, 0));
		Assert.assertFalse(ArrayUtils.equalsAt("<HTML>".getBytes(), arr2, 1));
		Assert.assertFalse(ArrayUtils.equalsAt("<HTML>".getBytes(), arr2, 2));
		Assert.assertFalse(ArrayUtils.equalsAt("<HTML>".getBytes(), arr2, 5));
		Assert.assertFalse(ArrayUtils.equalsAt("<HTML>".getBytes(), arr2, 10));

		Assert.assertFalse(ArrayUtils.equalsAt("<html>".getBytes(), arr2, 0));
		Assert.assertTrue(ArrayUtils.equalsAt("<html>".getBytes(), arr2, 1));
		Assert.assertFalse(ArrayUtils.equalsAt("<html>".getBytes(), arr2, 2));
		Assert.assertFalse(ArrayUtils.equalsAt("<html>".getBytes(), arr2, 5));
		Assert.assertFalse(ArrayUtils.equalsAt("<html>".getBytes(), arr2, 10));

		Assert.assertFalse(ArrayUtils.equalsAt("<HEAD>".getBytes(), arr, 1));
		Assert.assertFalse(ArrayUtils.equalsAt("<head>".getBytes(), arr, 1));
		Assert.assertFalse(ArrayUtils.equalsAt("<HEAD>".getBytes(), arr2, 1));
		Assert.assertFalse(ArrayUtils.equalsAt("<head>".getBytes(), arr2, 1));

		Assert.assertFalse(ArrayUtils.equalsAt("<HEAD>".getBytes(), ArrayUtils.CASE_INSENSITIVE, 192));
		Assert.assertFalse(ArrayUtils.equalsAt("<head>".getBytes(), ArrayUtils.CASE_INSENSITIVE, 192));
		Assert.assertFalse(ArrayUtils.equalsAt(new byte[] {(byte)192, (byte)193}, ArrayUtils.CASE_INSENSITIVE, 192));
		Assert.assertFalse(ArrayUtils.equalsAt(new byte[] {(byte)192, (byte)193}, ArrayUtils.CASE_INSENSITIVE, 192));

		/*
		 * equalsAtIgnoreCase().
		 */

		Assert.assertFalse(ArrayUtils.equalsAtIgnoreCase("<HTML>".getBytes(), arr, 0));
		Assert.assertTrue(ArrayUtils.equalsAtIgnoreCase("<HTML>".getBytes(), arr, 1));
		Assert.assertFalse(ArrayUtils.equalsAtIgnoreCase("<HTML>".getBytes(), arr, 2));
		Assert.assertFalse(ArrayUtils.equalsAtIgnoreCase("<HTML>".getBytes(), arr, 5));
		Assert.assertFalse(ArrayUtils.equalsAtIgnoreCase("<HTML>".getBytes(), arr, 10));

		Assert.assertFalse(ArrayUtils.equalsAtIgnoreCase("<html>".getBytes(), arr, 0));
		Assert.assertTrue(ArrayUtils.equalsAtIgnoreCase("<html>".getBytes(), arr, 1));
		Assert.assertFalse(ArrayUtils.equalsAtIgnoreCase("<html>".getBytes(), arr, 2));
		Assert.assertFalse(ArrayUtils.equalsAtIgnoreCase("<html>".getBytes(), arr, 5));
		Assert.assertFalse(ArrayUtils.equalsAtIgnoreCase("<html>".getBytes(), arr, 10));

		Assert.assertFalse(ArrayUtils.equalsAtIgnoreCase("<HTML>".getBytes(), arr2, 0));
		Assert.assertTrue(ArrayUtils.equalsAtIgnoreCase("<HTML>".getBytes(), arr2, 1));
		Assert.assertFalse(ArrayUtils.equalsAtIgnoreCase("<HTML>".getBytes(), arr2, 2));
		Assert.assertFalse(ArrayUtils.equalsAtIgnoreCase("<HTML>".getBytes(), arr2, 5));
		Assert.assertFalse(ArrayUtils.equalsAtIgnoreCase("<HTML>".getBytes(), arr2, 10));

		Assert.assertFalse(ArrayUtils.equalsAtIgnoreCase("<html>".getBytes(), arr2, 0));
		Assert.assertTrue(ArrayUtils.equalsAtIgnoreCase("<html>".getBytes(), arr2, 1));
		Assert.assertFalse(ArrayUtils.equalsAtIgnoreCase("<html>".getBytes(), arr2, 2));
		Assert.assertFalse(ArrayUtils.equalsAtIgnoreCase("<html>".getBytes(), arr2, 5));
		Assert.assertFalse(ArrayUtils.equalsAtIgnoreCase("<html>".getBytes(), arr2, 10));

		Assert.assertFalse(ArrayUtils.equalsAtIgnoreCase("<HEAD>".getBytes(), arr, 1));
		Assert.assertFalse(ArrayUtils.equalsAtIgnoreCase("<head>".getBytes(), arr, 1));
		Assert.assertFalse(ArrayUtils.equalsAtIgnoreCase("<HEAD>".getBytes(), arr2, 1));
		Assert.assertFalse(ArrayUtils.equalsAtIgnoreCase("<head>".getBytes(), arr2, 1));

		Assert.assertFalse(ArrayUtils.equalsAtIgnoreCase("<HEAD>".getBytes(), ArrayUtils.CASE_INSENSITIVE, 192));
		Assert.assertFalse(ArrayUtils.equalsAtIgnoreCase("<head>".getBytes(), ArrayUtils.CASE_INSENSITIVE, 192));
		Assert.assertTrue(ArrayUtils.equalsAtIgnoreCase(new byte[] {(byte)192, (byte)193}, ArrayUtils.CASE_INSENSITIVE, 192));
		Assert.assertTrue(ArrayUtils.equalsAtIgnoreCase(new byte[] {(byte)192, (byte)193}, ArrayUtils.CASE_INSENSITIVE, 192));
	}

	/**
	 * Test indexOf() and indexOfIgnoreCase().
	 */
	@Test
	public void test_arrayutils_indexof() {
		ArrayUtils arrayUtils = new ArrayUtils();
		Assert.assertNotNull(arrayUtils);

		byte[] arr;
		byte[] arr2;
		Object[][] cases;

		arr = " <HTML> <html> <HtMl> <HTML> <html> ".getBytes();
		arr2 = "".getBytes();

		/*
		 * indexOf().
		 */

		cases = new Object[][] {
				{
					-1, "".getBytes(), new Integer[] {0, arr.length}
				},
				{
					-1, "<HTLM>".getBytes(), new Integer[] {0, arr.length}
				},
				{
					-1, "<HTLM<".getBytes(), new Integer[] {0, arr.length}
				},
				{
					-1, ">HTLM>".getBytes(), new Integer[] {0, arr.length}
				},
				{
					3, "T".getBytes(), new Integer[] {0, 3}
				},
				{
					24, "T".getBytes(), new Integer[] {4, 24}
				},
				{
					-1, "T".getBytes(), new Integer[] {25, arr.length}
				},
				{
					10, "t".getBytes(), new Integer[] {0, 10}
				},
				{
					17, "t".getBytes(), new Integer[] {11, 17}
				},
				{
					31, "t".getBytes(), new Integer[] {18, 31}
				},
				{
					-1, "t".getBytes(), new Integer[] {32, arr.length}
				},
				{
					1, "<HTML>".getBytes(), new Integer[] {0, 1}
				},
				{
					22, "<HTML>".getBytes(), new Integer[] {2, 22}
				},
				{
					-1, "<HTML>".getBytes(), new Integer[] {23, arr.length}
				},
				{
					8, "<html>".getBytes(), new Integer[] {0, 8}
				},
				{
					29, "<html>".getBytes(), new Integer[] {9, 29}
				},
				{
					-1, "<html>".getBytes(), new Integer[] {30, arr.length}
				},
				{
					15, "<HtMl>".getBytes(), new Integer[] {0, 15}
				},
				{
					-1, "<HtMl>".getBytes(), new Integer[] {16, arr.length}
				}
		};

		int pos;
		byte[] subArr;
		Integer[] interval;
		int fIdx, tIdx;

		for (int i=0; i<cases.length; ++i) {
			pos = (Integer)cases[i][0];
			subArr = (byte[])cases[i][1];
			interval = (Integer[])cases[i][2];
			fIdx = interval[0];
			tIdx = interval[1];
			while (fIdx <= tIdx) {
				// debug
				//System.out.println(fIdx);
				Assert.assertEquals(pos, ArrayUtils.indexOf(subArr, arr, fIdx));
				++fIdx;
			}
		}

		Assert.assertEquals(-1, ArrayUtils.indexOf("<HTML>".getBytes(), arr2, 0));

		/*
		 * indexOfIgnoreCase().
		 */

		cases = new Object[][] {
				{
					-1, "".getBytes(), new Integer[] {0, arr.length}
				},
				{
					-1, "<HTLM>".getBytes(), new Integer[] {0, arr.length}
				},
				{
					-1, "<HTLM<".getBytes(), new Integer[] {0, arr.length}
				},
				{
					-1, ">HTLM>".getBytes(), new Integer[] {0, arr.length}
				},
				{
					3, "T".getBytes(), new Integer[] {0, 3}
				},
				{
					10, "T".getBytes(), new Integer[] {4, 10}
				},
				{
					17, "T".getBytes(), new Integer[] {11, 17}
				},
				{
					24, "T".getBytes(), new Integer[] {18, 24}
				},
				{
					31, "T".getBytes(), new Integer[] {25, 31}
				},
				{
					-1, "T".getBytes(), new Integer[] {32, arr.length}
				},
				{
					3, "t".getBytes(), new Integer[] {0, 3}
				},
				{
					10, "t".getBytes(), new Integer[] {4, 10}
				},
				{
					17, "t".getBytes(), new Integer[] {11, 17}
				},
				{
					24, "t".getBytes(), new Integer[] {18, 24}
				},
				{
					31, "t".getBytes(), new Integer[] {25, 31}
				},
				{
					-1, "t".getBytes(), new Integer[] {32, arr.length}
				},
				{
					1, "<HTML>".getBytes(), new Integer[] {0, 1}
				},
				{
					8, "<HTML>".getBytes(), new Integer[] {2, 8}
				},
				{
					15, "<HTML>".getBytes(), new Integer[] {9, 15}
				},
				{
					22, "<HTML>".getBytes(), new Integer[] {16, 22}
				},
				{
					29, "<HTML>".getBytes(), new Integer[] {23, 29}
				},
				{
					-1, "<HTML>".getBytes(), new Integer[] {30, arr.length}
				},
				{
					-1, "<htlm>".getBytes(), new Integer[] {0, arr.length}
				},
				{
					-1, "<htlm<".getBytes(), new Integer[] {0, arr.length}
				},
				{
					-1, ">htlm>".getBytes(), new Integer[] {0, arr.length}
				},
				{
					1, "<html>".getBytes(), new Integer[] {0, 1}
				},
				{
					8, "<html>".getBytes(), new Integer[] {2, 8}
				},
				{
					15, "<html>".getBytes(), new Integer[] {9, 15}
				},
				{
					22, "<html>".getBytes(), new Integer[] {16, 22}
				},
				{
					29, "<html>".getBytes(), new Integer[] {23, 29}
				},
				{
					-1, "<html>".getBytes(), new Integer[] {30, arr.length}
				},
				{
					1, "<HtMl>".getBytes(), new Integer[] {0, 1}
				},
				{
					8, "<HtMl>".getBytes(), new Integer[] {2, 8}
				},
				{
					15, "<HtMl>".getBytes(), new Integer[] {9, 15}
				},
				{
					22, "<HtMl>".getBytes(), new Integer[] {16, 22}
				},
				{
					29, "<HtMl>".getBytes(), new Integer[] {23, 29}
				},
				{
					-1, "<HtMl>".getBytes(), new Integer[] {30, arr.length}
				}
		};

		for (int i=0; i<cases.length; ++i) {
			pos = (Integer)cases[i][0];
			subArr = (byte[])cases[i][1];
			interval = (Integer[])cases[i][2];
			fIdx = interval[0];
			tIdx = interval[1];
			while (fIdx <= tIdx) {
				// debug
				//System.out.println(fIdx);
				Assert.assertEquals(pos, ArrayUtils.indexOfIgnoreCase(subArr, arr, fIdx));
				++fIdx;
			}
		}

	}

	/**
	 * Test split().
	 */
	@Test
	public void test_arrayutils_split() {
		ArrayUtils arrayUtils = new ArrayUtils();
		Assert.assertNotNull(arrayUtils);

		Object[][] cases = new Object[][] {
				{
					0, 1,
					"".getBytes(),
					"".getBytes(),
					new byte[][] {
							"".getBytes()
					}
				},
				{
					0, "One,Two,Three,Four,Five".getBytes().length,
					".".getBytes(),
					"One,Two,Three,Four,Five".getBytes(),
					new byte[][] {
						"One,Two,Three,Four,Five".getBytes()
					}
				},
				{
					0, "One,Two,Three,Four,Five".getBytes().length,
					",".getBytes(),
					"One,Two,Three,Four,Five".getBytes(),
					new byte[][] {
						"One".getBytes(),
						"Two".getBytes(),
						"Three".getBytes(),
						"Four".getBytes(),
						"Five".getBytes()
					}
				},
				{
					0, ",,One,Two,,Three,,Four,Five,,".getBytes().length,
					",".getBytes(),
					",,One,Two,,Three,,Four,Five,,".getBytes(),
					new byte[][] {
						"".getBytes(),
						"".getBytes(),
						"One".getBytes(),
						"Two".getBytes(),
						"".getBytes(),
						"Three".getBytes(),
						"".getBytes(),
						"Four".getBytes(),
						"Five".getBytes(),
						"".getBytes(),
						"".getBytes()
					}
				},
				{
					2, ",,One,Two,,Three,,Four,Five".getBytes().length,
					",".getBytes(),
					",,One,Two,,Three,,Four,Five,,".getBytes(),
					new byte[][] {
						"One".getBytes(),
						"Two".getBytes(),
						"".getBytes(),
						"Three".getBytes(),
						"".getBytes(),
						"Four".getBytes(),
						"Five".getBytes(),
					}
				},
				{
					0, ",,One, Two, ,Three,, Four, Five,,".getBytes().length,
					", ".getBytes(),
					",,One, Two, ,Three,, Four, Five,,".getBytes(),
					new byte[][] {
						",,One".getBytes(),
						"Two".getBytes(),
						",Three,".getBytes(),
						"Four".getBytes(),
						"Five,,".getBytes()
					}
				},
				{
					0, ",,One, Two, ,Three,, Four, Five,, ".getBytes().length,
					", ".getBytes(),
					",,One, Two, ,Three,, Four, Five,, ".getBytes(),
					new byte[][] {
						",,One".getBytes(),
						"Two".getBytes(),
						",Three,".getBytes(),
						"Four".getBytes(),
						"Five,".getBytes(),
						"".getBytes()
					}
				},
		};

		int fIdx, tIdx;
		byte[] subArr;
		byte[] arr;
		byte[][] expected;
		List<byte[]> result;
		for (int i=0; i<cases.length; ++i) {
			// debug
			System.out.println(i);
			fIdx = (Integer)cases[i][0];
			tIdx = (Integer)cases[i][1];
			subArr = (byte[])cases[i][2];
			arr = (byte[])cases[i][3];
			expected = (byte[][])cases[i][4];
			result = ArrayUtils.split(arr, subArr, fIdx, tIdx);
			Assert.assertNotNull(result);
			Assert.assertEquals(expected.length, result.size());
			for (int j=0; j<expected.length; ++j) {
				// debug
				System.out.println(new String(result.get(j)));
				Assert.assertArrayEquals(expected[j], result.get(j));
			}
		}

		ArrayUtils.split("".getBytes(), "O".getBytes(), 0, 1);

		try {
			ArrayUtils.split("".getBytes(), "O".getBytes(), 1, 0);
			Assert.fail("Exception expected!");
		} catch (IllegalArgumentException e) {
		}
	}

}
