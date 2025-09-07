package org.badmintonchain.model.entity;


import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name="services")
@Getter
@Setter
public class ServicesEntity extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "service_name", nullable = false)
    private String serviceName;
    @Column(name = "service_type")
    private String serviceType;
    @Column(name = "description")
    private String description;
    @Column(name = "unit_price", nullable = false)
    private BigDecimal unitPrice;
}
