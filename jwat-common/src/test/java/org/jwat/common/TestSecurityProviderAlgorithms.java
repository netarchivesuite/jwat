package org.jwat.common;

import java.security.MessageDigest;
import java.security.Provider;

public class TestSecurityProviderAlgorithms {

	public static void main(String[] args) throws Exception {
		Provider[] providers = SecurityProviderTools.getSecurityProviders();
		if (!SecurityProviderTools.isProviderAvailable(providers, "BC")) {
			SecurityProviderTools.loadBCProvider();
		}
		SecurityProviderAlgorithms spa = SecurityProviderAlgorithms.getInstanceFor(MessageDigest.class);
		System.out.println(spa.getAlgorithmList());
		System.out.println(spa.getAlgorithmListGrouped());
	}

}
