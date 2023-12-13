package org.jwat.common;

import java.security.Provider;
import java.security.Provider.Service;
import java.security.Security;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class SecurityProviderAlgorithms {

	public Map<String, Set<String>> digestAlgos = new TreeMap<>();
	public Set<String> digestAliases = new TreeSet<>();
	public Set<String> digestAndAliases = new TreeSet<>();

	protected SecurityProviderAlgorithms() {
	}

	public static SecurityProviderAlgorithms getInstanceFor(Class<?> algoritmClass) {
		SecurityProviderAlgorithms spa = new SecurityProviderAlgorithms();
		spa.getAlgorithms(algoritmClass.getSimpleName());
		return spa;
	}

	public void getAlgorithms(final String algoritmClassName) {
		//final String algoritmClassName = MessageDigest.class.getSimpleName();
		final String aliasPrefix = "Alg.Alias." + algoritmClassName + ".";
		final int aliasPrefixLen = aliasPrefix.length();
		Provider[] providers = Security.getProviders();
		String providerAlias;
		if (SecurityProviderTools.isProviderAvailable(providers, "BC")) {
			providerAlias = "BC";
		}
		else {
			providerAlias = "SUN";
		}
		Provider provider = Security.getProvider(providerAlias);
		Set<Service> services = provider.getServices();
		services.stream().forEach(service -> {
			String algorithm;
			Set<String> aliases;
			if (algoritmClassName.equalsIgnoreCase(service.getType())) {
				algorithm = service.getAlgorithm();
				char[] charArr = algorithm.toCharArray();
				int charIdx = charArr.length - 1;
				char c;
				boolean b = true;
				while (b && charIdx >= 0) {
					c = charArr[charIdx--];
					b = ((c >= '0' && c<= '9') || c == '.');
				}
				if (charIdx != -1 && !(charIdx == 1 && algorithm.startsWith("OID."))) {
					aliases = digestAlgos.get(algorithm);
					if (aliases == null) {
						digestAlgos.put(algorithm, new TreeSet<String>());
						digestAndAliases.add(algorithm);
					}
				}
			}
		});
		provider.keySet().stream().map(Object::toString).filter(s -> s.startsWith(aliasPrefix)).forEach(s -> {
			String alias = s.substring(aliasPrefixLen);
			String algorithm = provider.get(s).toString();
			if (alias.compareToIgnoreCase(algorithm) != 0) {
				char[] charArr = alias.toCharArray();
				int charIdx = charArr.length - 1;
				char c;
				boolean b = true;
				while (b && charIdx >= 0) {
					c = charArr[charIdx--];
					b = ((c >= '0' && c<= '9') || c == '.');
				}
				if (charIdx != -1 && !(charIdx == 1 && alias.startsWith("OID."))) {
					Set<String> algorithms = digestAlgos.get(algorithm);
					if (algorithms != null) {
						algorithms.add(alias);
						digestAliases.add(alias);
						digestAndAliases.add(alias);
					}
				}
			}
		});
	}

	public String getAlgorithmList() {
		final StringBuffer sb = new StringBuffer();
		digestAlgos.entrySet().forEach(e -> {
			Set<String> algorithms = e.getValue();
			if (sb.length() > 0) {
				sb.append(", ");
			}
			sb.append(e.getKey());
			Iterator<String> aliasIter = algorithms.iterator();
			if (aliasIter.hasNext()) {
				sb.append(" (");
				sb.append(aliasIter.next());
				while (aliasIter.hasNext()) {
					sb.append(", ");
					sb.append(aliasIter.next());
				}
				sb.append(")");
			}
		});
		return sb.toString();
	}

	public String getAlgorithmListGrouped() {
		final StringBuffer sb = new StringBuffer();
		Iterator<Entry<String, Set<String>>> iter = digestAlgos.entrySet().iterator();
		Entry<String, Set<String>> entry;
		String name;
		Set<String> algorithms;
		char[] groupNameCharArr = null;
		char[] nameCharArr;
		int gnStrPos = 0;
		int c;
		int idx;
		boolean bPrefix;
		boolean bComma;
		while (iter.hasNext()) {
			entry = iter.next();
			name = entry.getKey();
			algorithms = entry.getValue();
			bPrefix = false;
			if (sb.length() > 0) {
				bComma = true;
				if (gnStrPos > 0) {
					nameCharArr = name.toCharArray();
					if (nameCharArr.length >= gnStrPos) {
						idx = gnStrPos - 1;
						while (idx >= 0 && groupNameCharArr[idx] == nameCharArr[idx]) {
							--idx;
						}
						if (idx == -1) {
							if (nameCharArr.length > gnStrPos) {
								c = nameCharArr[gnStrPos];
								if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')) {
									bComma = false;
								}
							}
						}
						else {
							bComma = false;
						}
						//System.out.println(idx + " " + name);
					}
					else {
						bComma = false;
					}
				}
				if (bComma) {
					sb.append(", ");
				}
				else {
					sb.append("\n");
					bPrefix = true;
				}
			}
			else {
				bPrefix = true;
			}
			if (bPrefix) {
				groupNameCharArr = name.toCharArray();
				idx = 0;
				while (bPrefix) {
					if (idx < groupNameCharArr.length) {
						c = groupNameCharArr[idx];
						if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')) {
							++idx;
						}
						else {
							gnStrPos = idx;
							bPrefix = false;
						}
					}
					else {
						gnStrPos = idx;
						bPrefix = false;
					}
				}
				//System.out.println(gnStrPos + " " + name);
			}
			sb.append(name);
			Iterator<String> aliasIter = algorithms.iterator();
			if (aliasIter.hasNext()) {
				sb.append(" (");
				sb.append(aliasIter.next());
				while (aliasIter.hasNext()) {
					sb.append(", ");
					sb.append(aliasIter.next());
				}
				sb.append(")");
			}
		}
		return sb.toString();
	}


}
