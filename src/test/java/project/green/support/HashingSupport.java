package project.green.support;

import project.green.configuration.SecurityConfiguration;
import project.green.service.HashingService;

public class HashingSupport {
    private static SecurityConfiguration securityConfiguration = new SecurityConfiguration();

    public static HashingService hashingService() {
        return securityConfiguration.hashingService(securityConfiguration.messageDigestSupplier());
    }
}
