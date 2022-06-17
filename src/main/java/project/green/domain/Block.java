package project.green.domain;

import java.time.ZonedDateTime;

public interface Block {
    String blockHash();
    String previousBlockHash();
    ZonedDateTime blockDateTime();
}
