package pl.ib.beauty.mapper;

import org.mapstruct.Mapper;
import pl.ib.beauty.model.dao.Address;
import pl.ib.beauty.model.dto.AddressDto;

@Mapper(componentModel = "spring")
public interface AddressMapper {


    AddressDto addressToDto(Address address);

    Address dtoToAddress(AddressDto addressDto);
}
