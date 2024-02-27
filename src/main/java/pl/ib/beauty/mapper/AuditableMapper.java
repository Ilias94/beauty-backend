package pl.ib.beauty.mapper;

import org.mapstruct.AfterMapping;
import org.mapstruct.MappingTarget;
import pl.ib.beauty.model.dao.Auditable;
import pl.ib.beauty.model.dto.AuditableDto;
import pl.ib.beauty.security.SecurityUtils;

public interface AuditableMapper<DAO extends Auditable, DTO extends AuditableDto> {
    @AfterMapping
    default void mapAuditableFields(DAO dao, @MappingTarget DTO.AuditableDtoBuilder<?, ?> dto) {
        if (!SecurityUtils.hasRole("ADMIN")) {
            dto.createdBy(null)
                    .createdDate(null)
                    .lastModifiedBy(null)
                    .lastModifiedDate(null);
        }
    }
}
