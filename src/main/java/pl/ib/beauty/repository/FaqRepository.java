package pl.ib.beauty.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.stereotype.Repository;
import pl.ib.beauty.model.dao.Faq;

import java.util.List;
import java.util.UUID;

@Repository
public interface FaqRepository extends JpaRepository<Faq, UUID> {
    @NativeQuery("""
            SELECT * FROM beautypg.faq
            WHERE embedding <=> CAST(?1 AS vector) < ?2
            ORDER BY embedding <=> CAST(?1 AS vector)
            """)
    List<Faq> findByEmbedding(String embedding, double distance);
}
