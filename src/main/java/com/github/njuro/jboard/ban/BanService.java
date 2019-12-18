package com.github.njuro.jboard.ban;

import com.github.njuro.jboard.common.Constants;
import com.github.njuro.jboard.post.Post;
import com.github.njuro.jboard.user.User;
import com.github.njuro.jboard.user.UserService;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class BanService {

  private final BanRepository banRepository;
  private final UserService userService;

  @Autowired
  public BanService(BanRepository banRepository, UserService userService) {
    this.banRepository = banRepository;
    this.userService = userService;
  }

  public boolean hasActiveBan(String ip) {
    return !banRepository.findByIpAndStatus(ip, BanStatus.ACTIVE).isEmpty();
  }

  public Ban warn(String ip, String reason, Post post) {
    return createBan(ip, reason, null, post, BanStatus.WARNING);
  }

  public Ban ban(String ip, String reason, LocalDateTime end, Post post) {
    return createBan(ip, reason, end, post, BanStatus.ACTIVE);
  }

  private Ban createBan(
      String ip, String reason, LocalDateTime end, Post post, BanStatus banStatus) {
    User loggedUser = userService.getCurrentUser();
    if (loggedUser == null) {
      throw new IllegalArgumentException("No user is logged in!");
    }

    LocalDateTime NOW = LocalDateTime.now();

    if (end != null && end.isBefore(NOW)) {
      throw new IllegalArgumentException("End datetime must be in the future");
    }

    Ban ban =
        Ban.builder()
            .ip(ip)
            .status(banStatus)
            .post(post)
            .bannedBy(loggedUser)
            .reason(reason)
            .start(NOW)
            .end(end)
            .build();

    return banRepository.save(ban);
  }

  public Ban unban(Ban ban, String reason) {
    User loggedUser = userService.getCurrentUser();
    if (loggedUser == null) {
      throw new IllegalArgumentException("No user is logged in!");
    }

    ban.setUnbannedBy(loggedUser);
    ban.setUnbanReason(reason);

    return unban(ban, false);
  }

  private Ban unban(Ban ban, boolean expired) {
    if (!ban.getStatus().equals(BanStatus.ACTIVE)) {
      throw new IllegalArgumentException("Attempting to unban non-active createBan!");
    }

    ban.setStatus(expired ? BanStatus.EXPIRED : BanStatus.UNBANNED);
    return banRepository.save(ban);
  }

  @Scheduled(fixedRateString = Constants.EXPIRED_BANS_CHECK_PERIOD)
  public void checkExpiredBans() {
    List<Ban> expiredBans =
        banRepository.findByStatusAndEndBefore(BanStatus.ACTIVE, LocalDateTime.now());
    expiredBans.forEach(ban -> unban(ban, true));
  }
}