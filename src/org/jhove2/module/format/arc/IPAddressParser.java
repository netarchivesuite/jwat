/**
 * JHOVE2 - Next-generation architecture for format-aware characterization
 *
 * Copyright (c) 2009 by The Regents of the University of California,
 * Ithaka Harbors, Inc., and The Board of Trustees of the Leland Stanford
 * Junior University.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * o Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 *
 * o Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * o Neither the name of the University of California/California Digital
 *   Library, Ithaka Harbors/Portico, or Stanford University, nor the names of
 *   its contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package org.jhove2.module.format.arc;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.regex.Pattern;

/**
 * IP address parser.
 *
 * @author lbihanic, selghissassi
 */
public final class IPAddressParser {

    /** Regular expression for Ipv4 or Ipv6 address. */
    private static final String IP_ADDRESS_REG_EXP =
        "([0-9a-fA-F]{0,4}:){0,6}(" +             // Optional IPv6 start
        "([0-9a-fA-F]{0,4}:[0-9a-fA-F]{1,4})|" + // True IPv6 address or
        "(([0-9]{1,3}\\.){3}[0-9]{1,3}))";      // Standalone or mixed IPv4 address

    /** IpAddress compiled regex pattern. */
    private static final Pattern IP_ADDRESS_PATTERN = Pattern.compile(IP_ADDRESS_REG_EXP);

    /**
     * Checks the validity of an IP address. Supports both IP v4 and IP v6 formats.
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
    private IPAddressParser() {
    }

}
