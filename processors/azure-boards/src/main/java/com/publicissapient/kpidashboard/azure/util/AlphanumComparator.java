/*******************************************************************************
 * Copyright 2014 CapitalOne, LLC.
 * Further development Copyright 2022 Sapient Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package com.publicissapient.kpidashboard.azure.util;

import java.io.File;
import java.util.Comparator;

import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Service;

@Service
public class AlphanumComparator implements Comparator<File> {
	private boolean isDigit(char ch) {
		return ((ch >= 48) && (ch <= 57));
	}

	/**
	 * Gets a chunk of String that is passed to the method, starting from the
	 * location of marker.
	 *
	 * @param str
	 *            String input
	 * @param slength
	 *            string length
	 * @param marker
	 *            Marker
	 * @return String which is chunk of str
	 */
	private String getChunk(String str, int slength, int marker) {
		int theMarker = marker;
		StringBuilder chunk = new StringBuilder();
		char theChar = str.charAt(theMarker);
		chunk.append(theChar);
		theMarker++;
		if (isDigit(theChar)) {
			while (theMarker < slength) {
				theChar = str.charAt(theMarker);
				if (!isDigit(theChar)) {
					break;
				}
				chunk.append(theChar);
				theMarker++;
			}
		} else {
			while (theMarker < slength) {
				theChar = str.charAt(theMarker);
				if (isDigit(theChar)) {
					break;
				}
				chunk.append(theChar);
				theMarker++;
			}
		}
		return chunk.toString();
	}

	/**
	 * Compares two files based on the chunk of the name which is a string, if the
	 * chunk contains digits then the method compares it char by char
	 *
	 * @param file1
	 *            this File to compare
	 * @param file2
	 *            that File to compare
	 * @return 0 if thisChunk and thatChunk are equal,-1 if thisChunk is smaller,1
	 *         if thisChunk is bigger
	 */
	@Override
	public int compare(File file1, File file2) {// NOSONAR
		String s1 = FilenameUtils.getBaseName(file1.getName());
		String s2 = FilenameUtils.getBaseName(file2.getName());
		if ((s1 == null) || (s2 == null)) {
			return 0;
		}

		int thisMarker = 0;
		int thatMarker = 0;
		int s1Length = s1.length();
		int s2Length = s2.length();

		while (thisMarker < s1Length && thatMarker < s2Length) {
			String thisChunk = getChunk(s1, s1Length, thisMarker);
			thisMarker += thisChunk.length();

			String thatChunk = getChunk(s2, s2Length, thatMarker);
			thatMarker += thatChunk.length();

			// If both chunks contain numeric characters, sort them numerically
			int result = 0;
			if (isDigit(thisChunk.charAt(0)) && isDigit(thatChunk.charAt(0))) {
				// Simple chunk comparison by length.
				int thisChunkLength = thisChunk.length();
				result = thisChunkLength - thatChunk.length();
				// If equal, the first different number counts
				if (result == 0) {
					for (int i = 0; i < thisChunkLength; i++) {
						result = thisChunk.charAt(i) - thatChunk.charAt(i);
						if (result != 0) {
							return result;
						}
					}
				}
			} else {
				result = thisChunk.compareTo(thatChunk);
			}

			if (result != 0) {
				return result;
			}
		}

		return s1Length - s2Length;
	}
}
