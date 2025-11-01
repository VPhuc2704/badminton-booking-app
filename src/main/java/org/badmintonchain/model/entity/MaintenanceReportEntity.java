package org.badmintonchain.model.entity;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "maintenance_report")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaintenanceReportEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "court_id", nullable = false)
    private CourtEntity court;

    @Column(nullable = false)
    private String reporterName;

    @Column(columnDefinition = "TEXT")
    private String description;
}
