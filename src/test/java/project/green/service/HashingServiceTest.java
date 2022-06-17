package project.green.service;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static project.green.support.HashingSupport.hashingService;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class HashingServiceTest {

    @Test
    public void test64ZeroStringHash() {
        String sixtyFourZeros = "0000000000000000000000000000000000000000000000000000000000000000";
        HashingService hashingService = hashingService();
        assertEquals("c6fdd7a7f70862b36a26ccd14752268061e98103299b28fe7763bd9629926f4b", hashingService.hash(sixtyFourZeros.getBytes(StandardCharsets.UTF_8)));
    }
}
