package project.green.domain;

import project.green.service.HashingService;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static project.green.support.HashingSupport.hashingService;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class GenesisBlockTest {
    @Test
    public void previousBlockHashShouldBeHashOf64Zeros() {
        String sixtyFourZeros = "0000000000000000000000000000000000000000000000000000000000000000";
        HashingService hashingService = hashingService();
        GenesisBlock genesisBlock = GenesisBlock.INSTANCE;

        assertEquals(hashingService.hash(sixtyFourZeros.getBytes(StandardCharsets.UTF_8)), genesisBlock.previousBlockHash());
    }

    @Test
    public void genesisBlockHashShouldBeHashOf64Zeros() {
        String sixtyFourZeros = "0000000000000000000000000000000000000000000000000000000000000000";
        HashingService hashingService = hashingService();
        GenesisBlock genesisBlock = GenesisBlock.INSTANCE;

        assertEquals(hashingService.hash(sixtyFourZeros.getBytes(StandardCharsets.UTF_8)), genesisBlock.blockHash());
    }
}
