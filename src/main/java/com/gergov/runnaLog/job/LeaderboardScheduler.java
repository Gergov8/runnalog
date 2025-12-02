package com.gergov.runnaLog.job;

import com.gergov.runnaLog.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class LeaderboardScheduler {

    private final UserService userService;

    @Autowired
    public LeaderboardScheduler(UserService userService) {
        this.userService = userService;

    }

    @Scheduled(fixedRate = 60000)
    public void updateLiveLeaderboard() {

        userService.recalculateLeaderboard();

        System.out.println("Live leaderboard updated");
    }

    @Scheduled(cron =  "0 0 0 * * ?")
    public void resetLeaderboard  () {

        userService.resetLeaderboard();

        System.out.println("Daily leaderboard recalculated at 00:00");
    }

}
