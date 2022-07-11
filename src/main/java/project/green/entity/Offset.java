package project.green.entity;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;
import org.springframework.data.relational.core.mapping.Table;

@ToString
@Builder
@Data
@Table("offset_table")
public class Offset {
    private String account;
    private Long transactionId;
}
