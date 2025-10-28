package pl.ib.beauty.mapper;

import org.mapstruct.Mapper;
import pl.ib.beauty.model.dao.Video;
import pl.ib.beauty.model.dto.VideoDtoResponse;

@Mapper(componentModel = "spring")
public interface VideoMapper {

    VideoDtoResponse videoToDto(Video video);
}
