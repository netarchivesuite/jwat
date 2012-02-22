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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * TODO
 *
 * @param <T>
 *
 * @author nicl
 */
public class Diagnostics<T> {

	protected List<T> errors = new LinkedList<T>();

	protected List<T> warnings = new LinkedList<T>();

	public boolean hasErrors() {
		return !errors.isEmpty();
	}

	public boolean hasWarnings() {
		return !warnings.isEmpty();
	}

	public void addError(T d) {
		errors.add(d);
	}

	public void addWarning(T d) {
		warnings.add(d);
	}

	public List<T> getErrors() {
		return Collections.unmodifiableList(errors);
	}

	public List<T> getWarnings() {
		return Collections.unmodifiableList(warnings);
	}

}
