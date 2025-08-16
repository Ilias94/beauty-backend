package pl.ib.beauty.mapper;

import org.mapstruct.Mapper;
import pl.ib.beauty.model.dao.Address;
import pl.ib.beauty.model.dto.AddressDtoResponse;

@Mapper(componentModel = "spring")
public interface AddressMapper {


    AddressDtoResponse addressToDto(Address address);

    Address dtoToAddress(AddressDtoResponse addressDtoResponse);
}
