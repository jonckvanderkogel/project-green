package project.green.domain;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class GenesisBlock implements Block {
    private final ZonedDateTime blockDateTime = ZonedDateTime.of(LocalDateTime.of(0, 1, 1, 0, 0), ZoneId.of("GMT+01:00"));

    private GenesisBlock() {}

    public static final GenesisBlock INSTANCE = new GenesisBlock();

    /**
     * The hash of the GenesisBlock is calculated by concatenating the String value of the following hashes in order:
     * previousBlockHash
     * blockDateTime
     *
     * Then calculate the SHA3 256 hash of the concatenated string to get the GenesisBlock blockHash.
     * @return String
     */
    @Override
    public String blockHash() {
        return "eb3b10813d0ad61a028e77574e3b032dd5f7710ebd6fb8d2e19c05b7c8469822";
    }

    /**
     * Genesis block previous block hash is the SHA3 256 hash of a string of 64 zeros.
     * @return String
     */
    @Override
    public String previousBlockHash() {
        return "c6fdd7a7f70862b36a26ccd14752268061e98103299b28fe7763bd9629926f4b";
    }

    @Override
    public ZonedDateTime blockDateTime() {
        return blockDateTime;
    }
}
