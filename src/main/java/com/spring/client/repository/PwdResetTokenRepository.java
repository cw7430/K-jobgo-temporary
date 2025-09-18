package com.spring.client.repository;

import com.spring.client.entity.PwdResetToken;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface PwdResetTokenRepository extends JpaRepository<PwdResetToken, Long> {

  Optional<PwdResetToken> findByTokenHash(String tokenHash);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select t from PwdResetToken t where t.tokenHash = :hash")
  Optional<PwdResetToken> findForUpdateByTokenHash(@Param("hash") String hash);

  // (선택) 만료/사용 토큰 주기적 정리용
  @Modifying
  @Query("delete from PwdResetToken t where t.expiresAt < :now or t.usedAt is not null")
  int deleteExpiredOrUsed(@Param("now") LocalDateTime now);
}
