package project.green.entity;

import project.green.domain.Block;

import java.time.ZonedDateTime;

public class PaymentTransaction implements Block {

    @Override
    public String blockHash() {
        return null;
    }

    @Override
    public String previousBlockHash() {
        return null;
    }

    @Override
    public ZonedDateTime blockDateTime() {
        return null;
    }
}
