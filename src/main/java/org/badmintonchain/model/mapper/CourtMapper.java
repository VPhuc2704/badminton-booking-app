package org.badmintonchain.model.mapper;

import org.badmintonchain.model.dto.CourtDTO;
import org.badmintonchain.model.entity.CourtEntity;
import org.badmintonchain.model.enums.CourtStatus;

public class CourtMapper {
    public static CourtDTO toCourtDTO(CourtEntity courtEntity) {
        CourtDTO dto = new CourtDTO();
        dto.setId(courtEntity.getId());
        dto.setCourtName(courtEntity.getCourtName());
        dto.setCourtType(courtEntity.getCourtType());
        dto.setHourlyRate(courtEntity.getHourlyRate());
        dto.setDescription(courtEntity.getDescription());
        dto.setImages(courtEntity.getImages());
        dto.setIsActive(courtEntity.getIsActive());
        dto.setStatus(courtEntity.getStatus());
        return dto;
    }

    public static CourtEntity toCourtEntity(CourtDTO dto) {
        CourtEntity entity = new CourtEntity();
        entity.setCourtName(dto.getCourtName());
        entity.setCourtType(dto.getCourtType());
        entity.setHourlyRate(dto.getHourlyRate());
        entity.setDescription(dto.getDescription());
        entity.setImages(dto.getImages());
        entity.setIsActive(dto.getIsActive());
        entity.setStatus(dto.getStatus() != null ? dto.getStatus() : CourtStatus.AVAILABLE);
        return entity;
    }

}
