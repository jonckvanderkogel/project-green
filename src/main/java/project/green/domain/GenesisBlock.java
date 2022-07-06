package project.green.domain;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class GenesisBlock implements Block {
    private final ZonedDateTime blockDateTime = ZonedDateTime.of(LocalDateTime.of(0, 1, 1, 0, 0), ZoneId.of("GMT+01:00"));

    private GenesisBlock() {}

    public static final GenesisBlock INSTANCE = new GenesisBlock();

    /**
     * Genesis block hash is the SHA3 256 hash of a string of 64 zeros.
     * @return String
     */
    @Override
    public String blockHash() {
        return "c6fdd7a7f70862b36a26ccd14752268061e98103299b28fe7763bd9629926f4b";
    }

    /**
     * Genesis block previous block hash the same as the blockHash, there is nothing before it...
     * @return String
     */
    @Override
    public String previousBlockHash() {
        return blockHash();
    }

    @Override
    public ZonedDateTime blockDateTime() {
        return blockDateTime;
    }
}
