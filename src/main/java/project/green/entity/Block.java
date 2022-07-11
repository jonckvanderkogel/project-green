package project.green.entity;

import java.time.ZonedDateTime;

public interface Block {
    String blockHash();
    String previousBlockHash();
    ZonedDateTime blockDateTime();
}
