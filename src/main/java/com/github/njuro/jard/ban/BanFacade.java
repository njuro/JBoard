package com.github.njuro.jard.ban;

import com.github.njuro.jard.common.Constants;
import com.github.njuro.jard.user.User;
import com.github.njuro.jard.user.UserService;
import com.github.njuro.jard.utils.validation.FormValidationException;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class BanFacade {

  private final BanService banService;
  private final UserService userService;

  @Autowired
  public BanFacade(BanService banService, UserService userService) {
    this.banService = banService;
    this.userService = userService;
  }

  public Ban createBan(BanForm banForm) {
    User loggedUser = userService.getCurrentUser();
    if (loggedUser == null) {
      throw new FormValidationException("No user is logged in");
    }

    if (banService.hasActiveBan(banForm.getIp())) {
      throw new FormValidationException("There is already active ban on this IP");
    }

    Ban ban = banForm.toBan();
    ban.setBannedBy(loggedUser);
    ban.setValidFrom(LocalDateTime.now());

    if (ban.getStatus() == BanStatus.WARNING) {
      ban.setValidTo(null);
    }

    return banService.saveBan(ban);
  }

  public Ban getActiveBan(String ip) {
    return banService.getActiveBan(ip);
  }

  public List<Ban> getAllBans() {
    List<Ban> bans = banService.getAllBans();
    bans.sort(Comparator.comparing(Ban::getValidFrom).reversed());
    return bans;
  }

  public Ban resolveBan(UUID id) {
    return banService.resolveBan(id);
  }

  public Ban editBan(Ban oldBan, BanForm banForm) {
    oldBan.setReason(banForm.getReason());
    oldBan.setValidTo(banForm.getValidTo());

    return banService.saveBan(oldBan);
  }

  public Ban unban(Ban ban, UnbanForm unbanForm) {
    User loggedUser = userService.getCurrentUser();
    if (loggedUser == null) {
      throw new FormValidationException("No user is logged in!");
    }

    if (ban == null || ban.getStatus() != BanStatus.ACTIVE) {
      throw new FormValidationException("There is no active ban on this IP");
    }

    ban.setUnbannedBy(loggedUser);
    ban.setUnbanReason(unbanForm.getReason());
    ban.setStatus(BanStatus.UNBANNED);

    return banService.saveBan(ban);
  }

  @Scheduled(fixedRateString = Constants.EXPIRED_BANS_CHECK_PERIOD)
  public void unbanExpired() {
    List<Ban> expiredBans = banService.getExpiredBans();
    expiredBans.forEach(
        ban -> {
          ban.setStatus(BanStatus.EXPIRED);
          banService.saveBan(ban);
        });
  }
}