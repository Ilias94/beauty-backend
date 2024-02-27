package pl.ib.beauty.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.history.RevisionRepository;
import pl.ib.beauty.model.dao.Role;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long>, RevisionRepository<Role, Long, Integer> {
    Optional<Role> findByName(String name);
}
