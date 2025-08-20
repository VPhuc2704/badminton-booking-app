package org.badmintonchain.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "courts")
@Getter
@Setter
public class CourtEntity extends BaseEntity {
/*
    CREATE TABLE courts (
    court_id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    court_name VARCHAR(50) NOT NULL,
    court_type VARCHAR(50),
    status court_status DEFAULT 'available',
    hourly_rate DECIMAL(10,2) NOT NULL,
    location TEXT,
    capacity INT,
    description TEXT,
    images JSON,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
*/
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(name = "court_name", nullable = false, length = 100)
        private String courtName;

        @Column(name = "court_type", length = 50) // indoor, outdoor
        private String courtType;

        @Column(name = "hourly_rate", precision =  10, scale = 2)
        private BigDecimal hourlyRate;

        @Column(columnDefinition = "TEXT")
        private String description;

        // images: ["url1", "url2", "url3"]
        @JdbcTypeCode(SqlTypes.JSON)
        @Column(columnDefinition = "jsonb")
        private List<String> images;

        @Column(name = "is_active")
        private Boolean isActive = true;

}
