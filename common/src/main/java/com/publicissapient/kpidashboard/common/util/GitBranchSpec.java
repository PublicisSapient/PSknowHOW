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

package com.publicissapient.kpidashboard.common.util;

import java.util.StringTokenizer;
import java.util.regex.Pattern;

import lombok.Getter;

/**
 * The type Git branch spec.
 */
public class GitBranchSpec {

	@Getter
	private String name;

	/**
	 * Instantiates a new Git branch spec.
	 *
	 * @param name
	 *            the name
	 */
	public GitBranchSpec(String name) {
		setName(name);
	}

	private void setName(String name) {
		if (name == null) {
			throw new IllegalArgumentException();
		} else if (name.length() == 0) {
			this.name = "**";
		} else {
			this.name = name.trim();
		}
	}

	@Override
	public String toString() {
		return name;
	}

	/**
	 * Matches boolean.
	 *
	 * @param item
	 *            the item
	 * @return the boolean
	 */
	public boolean matches(String item) {
		return getPattern().matcher(item).matches();
	}

	private Pattern getPattern() {
		String expandedName = name;
		// use regex syntax directly if name starts with colon
		if (startWithColon(expandedName)) {
			String regexSubstring = expandedName.substring(1, expandedName.length());
			return Pattern.compile(regexSubstring);
		}
		// build a pattern into this builder
		StringBuilder builder = new StringBuilder(100);

		// for legacy reasons (sic) we do support various branch spec format to declare
		// remotes / branches
		builder.append("(refs/heads/");

		// if an unqualified branch was given, consider all remotes (with various
		// possible syntaxes)
		// so it will match branches from any remote repositories as the user probably
		// intended
		if (containsDoubleStarOrForwordSlash(expandedName)) {
			builder.append("|refs/remotes/|remotes/");
		} else {
			builder.append("|refs/remotes/[^/]+/|remotes/[^/]+/|[^/]+/");
		}
		builder.append(")?");

		// was the last token a wildcard?
		boolean foundWildcard = false;

		// split the string at the wildcards
		StringTokenizer tokenizer = new StringTokenizer(expandedName, "*", true);
		while (tokenizer.hasMoreTokens()) {
			String token = tokenizer.nextToken();

			// is this token is a wildcard?
			if ("*".equals(token)) {
				// yes, was the previous token a wildcard?
				if (foundWildcard) {
					// yes, we found "**"
					// match over any number of characters
					builder.append(".*");
					foundWildcard = false;
				} else {
					// no, set foundWildcard to true and go on
					foundWildcard = true;
				}
			} else {
				// no, was the previous token a wildcard?
				if (foundWildcard) {
					// yes, we found "*" followed by a non-wildcard
					// match any number of characters other than a "/"
					builder.append("[^/]*");
					foundWildcard = false;
				}
				// quote the non-wildcard token before adding it to the phrase
				builder.append(Pattern.quote(token));
			}
		}

		// if the string ended with a wildcard add it now
		if (foundWildcard) {
			builder.append("[^/]*");
		}

		return Pattern.compile(builder.toString());
	}

	private boolean containsDoubleStarOrForwordSlash(String expandedName) {
		return expandedName.contains("**") || expandedName.contains("/");
	}

	private boolean startWithColon(String expandedName) {
		return (expandedName.charAt(0) == ':') && (expandedName.length() > 1);
	}
}
