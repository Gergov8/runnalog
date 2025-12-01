package com.gergov.runnaLog.stats;

import com.gergov.runnaLog.run.model.Run;
import com.gergov.runnaLog.run.repository.RunRepository;
import com.gergov.runnaLog.stats.model.Stats;
import com.gergov.runnaLog.stats.repository.StatsRepository;
import com.gergov.runnaLog.stats.service.StatsService;
import com.gergov.runnaLog.user.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class StatsServiceUTest {

    @Mock
    private StatsRepository statsRepository;

    @Mock
    private RunRepository runRepository;

    @InjectMocks
    private StatsService statsService;

    @Test
    void createDefaultStats_ShouldSaveStatsWithCorrectDefaults() {

        User user = new User();
        user.setId(java.util.UUID.randomUUID());

        statsService.createDefaultStats(user);

        verify(statsRepository).save(argThat(stats ->
                stats.getUser().equals(user) &&
                        stats.getTotalRuns() == 0 &&
                        stats.getTotalDistance() == 0.0 &&
                        stats.getTotalDuration() == 0 &&
                        stats.getPb1km() == null &&
                        stats.getPb5km() == null &&
                        stats.getPb10km() == null &&
                        stats.getRunnerLevel() == 1 &&
                        stats.getStrides() == 100 &&
                        stats.getLastActivity() != null &&
                        stats.getLastActivity().isBefore(LocalDateTime.now().plusSeconds(1))
        ));
    }

    @Test
    void updateUserStatsAfterRun_ShouldUpdateStatsCorrectly() {

        Stats stats = new Stats();
        stats.setTotalRuns(5);
        stats.setTotalDistance(20.0);
        stats.setTotalDuration(120);
        stats.setStrides(100);
        stats.setPb1km("5:00");
        stats.setPb5km("5:30");
        stats.setPb10km("6:00");

        User user = new User();
        user.setStats(stats);

        double distance = 5.0;
        long totalSeconds = 1500;
        String pace = "5:00";

        statsService.updateUserStatsAfterRun(user, distance, totalSeconds, pace);

        verify(statsRepository).save(argThat(s ->
                s.getTotalRuns() == 6 &&
                        Math.abs(s.getTotalDistance() - 25.0) < 0.0001 &&
                        s.getTotalDuration() == 120 + 25 &&
                        s.getLastActivity() != null &&
                        s.getPb1km().equals("5:00") &&
                        s.getPb5km().equals("5:00") &&
                        s.getPb10km().equals("6:00") &&
                        s.getStrides() > 100
        ));
    }

    @Test
    void updateUserStatsAfterRun_ShouldSetNewPersonalBest_WhenPaceIsFaster() {

        Stats stats = new Stats();
        stats.setTotalRuns(10);
        stats.setTotalDistance(100.0);
        stats.setTotalDuration(600);
        stats.setStrides(500);
        stats.setPb1km("5:00");
        stats.setPb5km("4:50");
        stats.setPb10km("4:40");

        User user = new User();
        user.setStats(stats);

        double distance = 5.0;
        long totalSeconds = 1100;
        String pace = "3:40";

        statsService.updateUserStatsAfterRun(user, distance, totalSeconds, pace);

        verify(statsRepository).save(argThat(s ->
                s.getPb1km().equals("3:40") &&
                        s.getPb5km().equals("3:40") &&
                        s.getPb10km().equals("4:40")
        ));
    }

    @Test
    void updateUserStatsAfterRun_ShouldNotUpdatePersonalBest_WhenPaceIsSlower() {

        Stats stats = new Stats();
        stats.setPb1km("4:00");
        stats.setPb5km("4:20");
        stats.setPb10km("4:40");
        stats.setTotalRuns(0);
        stats.setTotalDistance(0.0);
        stats.setTotalDuration(0);
        stats.setStrides(0);

        User user = new User();
        user.setStats(stats);

        double distance = 10.0;
        long totalSeconds = 3000;
        String pace = "5:00";

        statsService.updateUserStatsAfterRun(user, distance, totalSeconds, pace);

        verify(statsRepository).save(argThat(s ->
                s.getPb1km().equals("4:00") &&
                        s.getPb5km().equals("4:20") &&
                        s.getPb10km().equals("4:40")
        ));
    }

    @Test
    void updateStrides_ShouldCalculateCorrectStrides() {

        Stats stats = new Stats();
        stats.setStrides(0);
        stats.setTotalRuns(0);
        stats.setTotalDistance(0.0);
        stats.setTotalDuration(0);
        stats.setStrides(0);

        User user = new User();
        user.setStats(stats);

        double distance = 10.0;
        long totalSeconds = 2700;

        statsService.updateUserStatsAfterRun(user, distance, totalSeconds, "4:30");

        int expected = 100 + 30 + 50;

        verify(statsRepository).save(argThat(s ->
                s.getStrides() == expected
        ));
    }

    @Test
    void updateUserStatsAfterDeleteRun_ShouldRecalculateStatsAndPB() {

        Stats stats = new Stats();
        stats.setTotalRuns(3);
        stats.setTotalDistance(15.0);
        stats.setTotalDuration(90);
        stats.setStrides(200);
        stats.setPb1km("4:00");
        stats.setPb5km("4:20");
        stats.setPb10km("4:40");

        User user = new User();
        user.setStats(stats);

        Run slowRun = new Run();
        slowRun.setPace("5:00");
        slowRun.setDistance(10.0);

        when(runRepository.findPublicRunsByUserOrderByPaceDesc(user))
                .thenReturn(List.of(slowRun));

        double distance = 5.0;
        long seconds = 1200;

        statsService.updateUserStatsAfterDeleteRun(user, distance, seconds);

        verify(statsRepository).save(argThat(s ->
                s.getTotalRuns() == 2 &&
                        Math.abs(s.getTotalDistance() - 10.0) < 0.001 &&
                        s.getTotalDuration() == 70 &&
                        s.getPb1km().equals("5:00") &&
                        s.getPb5km().equals("5:00") &&
                        s.getPb10km().equals("5:00")
        ));
    }


}
