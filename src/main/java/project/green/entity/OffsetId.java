package project.green.entity;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;

@EqualsAndHashCode
@RequiredArgsConstructor
public class OffsetId implements Serializable {
    private String account;
    private Long transactionId;
}
