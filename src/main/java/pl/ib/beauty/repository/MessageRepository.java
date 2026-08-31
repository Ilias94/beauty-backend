package pl.ib.beauty.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.ib.beauty.model.dao.Message;
import pl.ib.beauty.model.dao.User;

import java.util.List;
import java.util.Optional;

public interface MessageRepository extends JpaRepository<Message, Long> {

    @Query("""
            SELECT m FROM Message m
            WHERE (m.sender.id = :a AND m.recipient.id = :b)
               OR (m.sender.id = :b AND m.recipient.id = :a)
            ORDER BY m.sentAt ASC
            """)
    List<Message> findConversation(@Param("a") Long userId1, @Param("b") Long userId2);

    @Query("SELECT DISTINCT m.recipient FROM Message m WHERE m.sender.id = :userId")
    List<User> findRecipients(@Param("userId") Long userId);

    @Query("SELECT DISTINCT m.sender FROM Message m WHERE m.recipient.id = :userId")
    List<User> findSenders(@Param("userId") Long userId);

    @Query("""
            SELECT m FROM Message m
            WHERE (m.sender.id = :a AND m.recipient.id = :b)
               OR (m.sender.id = :b AND m.recipient.id = :a)
            ORDER BY m.sentAt DESC
            LIMIT 1
            """)
    Optional<Message> findLastMessage(@Param("a") Long userId1, @Param("b") Long userId2);

    long countBySenderIdAndRecipientIdAndReadFalse(Long senderId, Long recipientId);

    @Modifying
    @Query("UPDATE Message m SET m.read = true WHERE m.sender.id = :senderId AND m.recipient.id = :recipientId AND m.read = false")
    void markConversationAsRead(@Param("senderId") Long senderId, @Param("recipientId") Long recipientId);
}
