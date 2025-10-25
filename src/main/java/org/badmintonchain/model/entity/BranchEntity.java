package org.badmintonchain.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "branches")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BranchEntity extends  BaseEntity{

/*
        CREATE TABLE branches (
                id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                branch_name VARCHAR(100) NOT NULL,
        address TEXT,
        phone VARCHAR(20),
        manager_id INT UNIQUE,  -- user_id của STAFF quản lý chi nhánh này
        is_active BOOLEAN DEFAULT TRUE,
        create_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        update_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        FOREIGN KEY (manager_id) REFERENCES users(id) ON DELETE SET NULL
*/


        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(name = "branch_name", nullable = false, length = 100)
        private String branchName;

        @Column(columnDefinition = "TEXT")
        private String address;

        @Column(length = 20)
        private String phone;

        // Staff quản lý chi nhánh này
        @OneToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "manager_id", referencedColumnName = "id", unique = true)
        private UsersEntity manager;

        @Column(name = "is_active")
        private Boolean isActive = true;

        // Một chi nhánh có nhiều sân
        @OneToMany(mappedBy = "branch", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
        private List<CourtEntity> courts =  new ArrayList<>();

}
