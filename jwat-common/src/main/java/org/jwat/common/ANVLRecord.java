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
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Internet-Draft: draft-kunze-anvl-02.txt
 * http://tools.ietf.org/id/draft-kunze-anvl-02.txt
 *
 * @author nicl
 */
public class ANVLRecord {

    public static final String CRLF = "\r\n";

    protected List<String> list = new LinkedList<String>();

    protected Map<String, String> map = new HashMap<String, String>();

    public ANVLRecord() {
    }

    public void addLabelValue(String name, String value) {
        int c = -1;
        if (value == null) {
            throw new IllegalArgumentException("Invalid value!");
        } else if (value.length() > 0) {
            c = value.charAt(0);
        }
        if (name == null || name.length() == 0) {
            throw new IllegalArgumentException("Invalid name!");
        }
        if (c != -1 && c != ' ' && c != '\t') {
            list.add(name + ": " + value);
        } else {
            list.add(name + ":" + value);
        }
        map.put(name, value);
    }

    public void addValue(String value) {
        if (value == null || value.length() == 0) {
            throw new IllegalArgumentException("Invalid value!");
        }
        list.add(value);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        Iterator<String> iter = list.iterator();
        while (iter.hasNext()) {
            sb.append(iter.next());
            sb.append(CRLF);
        }
        sb.append(CRLF);
        return sb.toString();
    }

    public byte[] getBytes(String charsetName) throws UnsupportedEncodingException {
        return toString().getBytes(charsetName);
    }

    public byte[] getUTF8Bytes() throws UnsupportedEncodingException {
        return getBytes("UTF-8");
    }

    public ANVLRecord parse() {
        return null;
    }

}
