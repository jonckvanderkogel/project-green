package project.green.entity;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;
import org.springframework.data.relational.core.mapping.Table;

import javax.persistence.Id;

@ToString
@Builder
@Data
@Table("offset")
public class Offset {
    @Id
    private Long id;
    private String account;
    private Long transactionId;
}
