package pl.ib.beauty.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.history.RevisionRepository;
import pl.ib.beauty.model.dao.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long>, RevisionRepository<User, Long, Integer> {
//    @EntityGraph(attributePaths = "roleList", type = EntityGraph.EntityGraphType.LOAD)
    Optional<User> findByEmail(String email);
}
