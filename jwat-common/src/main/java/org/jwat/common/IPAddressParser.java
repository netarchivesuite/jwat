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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.regex.Pattern;

/**
 * IP address parser and format validator for Ipv4 and Ipv6.
 *
 * @author lbihanic, selghissassi
 */
public final class IPAddressParser {

    /** Regular expression for Ipv4 or Ipv6 address. */
    private static final String IP_ADDRESS_REG_EXP =
        "([0-9a-fA-F]{0,4}:){0,6}("                 // Optional IPv6 start
        + "([0-9a-fA-F]{0,4}:[0-9a-fA-F]{1,4})|"    // True IPv6 address or
        + "(([0-9]{1,3}\\.){3}[0-9]{1,3}))";        // Standalone or mixed IPv4 address

    /** IpAddress compiled regex pattern. */
    private static final Pattern IP_ADDRESS_PATTERN
                                        = Pattern.compile(IP_ADDRESS_REG_EXP);

    /**
     * Checks the validity of an IP address.
     * Supports both IP v4 and IP v6 formats.
     * @param ipAddress the IP address
     * @return true/false based on whether IP address is valid or not
     */
    public static InetAddress getAddress(String ipAddress){
        boolean isValid = (ipAddress == null) ? false
                            : IP_ADDRESS_PATTERN.matcher(ipAddress).matches();
        InetAddress inetAddress = null;
        if(isValid){
            try {
                inetAddress = InetAddress.getByName(ipAddress);
            } catch (UnknownHostException e) {
                isValid = false;
            }
        }
        return (isValid) ? inetAddress : null;
    }

    /**
     * No constructor for this utility class, static access only.
     */
    protected IPAddressParser() {
    }

}
