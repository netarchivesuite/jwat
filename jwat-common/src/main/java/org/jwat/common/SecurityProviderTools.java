package org.jwat.common;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.security.Provider;
import java.security.Security;

/**
 * Security Provider helper class.
 *
 * @author nicl
 */
public class SecurityProviderTools {

    /**
     * Constructor not used as of yet.
     */
    protected SecurityProviderTools() {
    }

    /**
     * Returns an array of available security providers.
     * @return array of available security providers
     */
    public static Provider[] getSecurityProviders() {
        return Security.getProviders();
    }

    /**
     * Check to see if a specific security provider is present in the supplied array of providers.
     * @param providers array of security providers
     * @param providerName provider name to check for
     * @return true if the provider is present in the array, false otherwise
     */
    public static boolean isProviderAvailable(Provider[] providers, String providerName) {
        int idx = 0;
        int len = providers.length;
        boolean bAvail = false;
        while (idx < len && !bAvail) {
            bAvail = providerName.equalsIgnoreCase(providers[idx].getName());
            ++idx;
        }
        return bAvail;
    }

    /**
     * Method to load the BouncyCastle(BC) security provider.
     * @return true if the BouncyCastle security provider is available, false otherwise
     */
    public static boolean loadBCProvider() {
        return loadProvider("org.bouncycastle.jce.provider.BouncyCastleProvider");
    }

    /**
     * Attempt to load a security provider and add it to the system list of providers.
     * @param providerName provider class name
     * @return true if the security provider is now available, false otherwise
     */
    public static boolean loadProvider(String providerName) {
        Class<?> bcProviderClass = null;
        Constructor<?> bcProviderConstructor = null;
        Object bcProviderObject = null;
        try {
            bcProviderClass = SecurityProviderTools.class.getClassLoader().loadClass(providerName);
        }
        catch (ClassNotFoundException e) {
            // Class will be null so no need to show exception.
        }
        try {
            if (bcProviderClass != null) {
                bcProviderConstructor = bcProviderClass.getConstructor();
            }
        }
        catch (NoSuchMethodException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        try {
            if (bcProviderConstructor != null) {
                bcProviderObject = bcProviderConstructor.newInstance();
            }
        }
        catch (InstantiationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (InvocationTargetException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if (bcProviderObject != null) {
            //Security.addProvider(new BouncyCastleProvider());
            Security.addProvider((Provider)bcProviderObject);
            return true;
        }
        return false;
    }

    /**
     * Output a list of security providers to the console.
     * @param providers list of security providers
     */
    public static void printSecurityProviders(Provider[] providers) {
        int idx = 0;
        int len = providers.length;
        Provider provider;
        while (idx < len) {
            provider = providers[idx];
            System.out.println(provider.getName() + " - " + provider.getInfo());
            ++idx;
        }
    }

}
