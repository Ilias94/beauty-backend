package pl.ib.beauty.repository;
import pl.ib.beauty.model.dao.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AddressRepository extends JpaRepository<Address, Long > {

    /** Street addresses that were saved without coordinates, so their course page has no map. */
    List<Address> findByLatIsNullAndStreetIsNotNullAndCityIsNotNull();
}
