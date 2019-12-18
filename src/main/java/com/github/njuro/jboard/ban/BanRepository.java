package com.github.njuro.jboard.ban;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BanRepository extends JpaRepository<Ban, Long> {

  List<Ban> findByIp(String ip);

  List<Ban> findByStatusAndEndBefore(BanStatus status, LocalDateTime dateTime);

  List<Ban> findByIpAndStatus(String ip, BanStatus status);
}