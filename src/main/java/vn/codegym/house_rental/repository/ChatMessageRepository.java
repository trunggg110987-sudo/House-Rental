package vn.codegym.house_rental.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.codegym.house_rental.model.ChatMessage;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    @Query("SELECT m FROM ChatMessage m WHERE m.house.id = :houseId " +
           "AND ((m.sender.id = :u1 AND m.receiver.id = :u2) OR (m.sender.id = :u2 AND m.receiver.id = :u1)) " +
           "ORDER BY m.createdAt ASC")
    List<ChatMessage> findConversation(
            @Param("houseId") Long houseId,
            @Param("u1") Long u1,
            @Param("u2") Long u2
    );

    @Query("SELECT COUNT(m) FROM ChatMessage m WHERE m.house.id = :houseId " +
           "AND m.receiver.id = :receiverId AND m.sender.id = :senderId AND m.isRead = false")
    long countUnreadMessages(
            @Param("houseId") Long houseId,
            @Param("receiverId") Long receiverId,
            @Param("senderId") Long senderId
    );

    @Modifying
    @Query("UPDATE ChatMessage m SET m.isRead = true WHERE m.house.id = :houseId " +
           "AND m.receiver.id = :receiverId AND m.sender.id = :senderId AND m.isRead = false")
    void markConversationAsRead(
            @Param("houseId") Long houseId,
            @Param("receiverId") Long receiverId,
            @Param("senderId") Long senderId
    );

    @Query("SELECT COUNT(m) FROM ChatMessage m WHERE m.house.id = :houseId " +
           "AND m.receiver.id = :receiverId AND m.isRead = false")
    long countTotalUnreadForReceiverInHouse(
            @Param("houseId") Long houseId,
            @Param("receiverId") Long receiverId
    );

    @Query("SELECT m FROM ChatMessage m WHERE m.house.id = :houseId ORDER BY m.createdAt DESC")
    List<ChatMessage> findByHouseIdOrderByCreatedAtDesc(@Param("houseId") Long houseId);

    @Query("SELECT DISTINCT m.house.id FROM ChatMessage m WHERE m.receiver.id = :userId OR m.sender.id = :userId")
    List<Long> findHouseIdsWithChatsForUser(@Param("userId") Long userId);
}