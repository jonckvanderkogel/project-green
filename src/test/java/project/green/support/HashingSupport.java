package project.green.support;

import project.green.configuration.SecurityConfiguration;
import project.green.service.HashingService;

public class HashingSupport {
    private static final SecurityConfiguration SECURITY_CONFIGURATION = new SecurityConfiguration();

    public static HashingService hashingService() {
        return SECURITY_CONFIGURATION.hashingService(SECURITY_CONFIGURATION.messageDigestSupplier());
    }
}
