package org.badmintonchain.repository;

import org.badmintonchain.model.entity.ServicesEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceRepository extends JpaRepository<ServicesEntity, Long> {

}
