package com.gergov.runnaLog.run;

import com.gergov.runnaLog.like.service.LikeService;
import com.gergov.runnaLog.run.model.Run;
import com.gergov.runnaLog.run.model.RunVisibility;
import com.gergov.runnaLog.run.repository.RunRepository;
import com.gergov.runnaLog.run.service.RunService;
import com.gergov.runnaLog.stats.service.StatsService;
import com.gergov.runnaLog.user.model.User;
import com.gergov.runnaLog.user.model.UserRole;
import com.gergov.runnaLog.web.dto.CreateRunRequest;
import com.gergov.runnaLog.web.dto.RunResponseDto;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RunServiceUTest {

    @Mock
    private RunRepository runRepository;
    @Mock
    private StatsService statsService;
    @Mock
    private LikeService likeService;
    @InjectMocks
    private RunService runService;

    @Test
    void createRun_ShouldSaveRunAndUpdateStats() {
        User user = new User();
        user.setUsername("John");

        CreateRunRequest req = new CreateRunRequest();
        req.setDistance(10.0);
        req.setDurationHours(0);
        req.setDurationMinutes(50);
        req.setDurationSeconds(0);
        req.setTitle("Morning Run");
        req.setDescription("Nice run");
        req.setVisibility(RunVisibility.PUBLIC);

        ArgumentCaptor<Run> runCaptor = ArgumentCaptor.forClass(Run.class);

        runService.createRun(req, user);

        verify(runRepository).save(runCaptor.capture());
        Run saved = runCaptor.getValue();

        assertEquals(10.0, saved.getDistance());
        assertEquals("Morning Run", saved.getTitle());
        assertEquals("Nice run", saved.getDescription());
        assertNotNull(saved.getCreatedOn());
        assertEquals(RunVisibility.PUBLIC, saved.getVisibility());
        assertEquals(user, saved.getUser());

        verify(statsService).updateUserStatsAfterRun(eq(user), eq(10.0), eq(3000L), anyString());
    }

    @Test
    void deleteRun_ShouldDeleteIfOwner() {
        UUID runId = UUID.randomUUID();

        User user = new User();
        user.setId(UUID.randomUUID());

        Run run = new Run();
        run.setId(runId);
        run.setUser(user);
        run.setDistance(5.0);
        run.setDuration(Duration.ofMinutes(25));

        when(runRepository.findById(runId)).thenReturn(Optional.of(run));

        runService.deleteRun(user, runId);

        verify(runRepository).delete(run);
        verify(statsService).updateUserStatsAfterDeleteRun(user, 5.0, 1500L);
    }

    @Test
    void deleteRun_ShouldAllowAdminEvenIfNotOwner() {
        UUID runId = UUID.randomUUID();

        User owner = new User();
        owner.setId(UUID.randomUUID());

        User admin = new User();
        admin.setId(UUID.randomUUID());
        admin.setRole(UserRole.ADMIN);

        Run run = new Run();
        run.setId(runId);
        run.setUser(owner);
        run.setDistance(8.0);
        run.setDuration(Duration.ofMinutes(40));

        when(runRepository.findById(runId)).thenReturn(Optional.of(run));

        runService.deleteRun(admin, runId);

        verify(runRepository).delete(run);
        verify(statsService).updateUserStatsAfterDeleteRun(admin, 8.0, 2400L);
    }

    @Test
    void deleteRun_ShouldThrowIfNotAllowed() {
        UUID runId = UUID.randomUUID();

        User owner = new User();
        owner.setId(UUID.randomUUID());

        User stranger = new User();
        stranger.setId(UUID.randomUUID());
        stranger.setRole(UserRole.USER);

        Run run = new Run();
        run.setUser(owner);

        when(runRepository.findById(runId)).thenReturn(Optional.of(run));

        assertThrows(org.springframework.security.access.AccessDeniedException.class,
                () -> runService.deleteRun(stranger, runId));

        verify(runRepository, never()).delete(any());
    }

    @Test
    void deleteRun_ShouldThrowIfNotFound() {
        UUID runId = UUID.randomUUID();

        when(runRepository.findById(runId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> runService.deleteRun(new User(), runId));
    }

    @Test
    void getVisibleRuns_ShouldReturnPublicRunsWhenUserNull() {
        List<Run> runs = List.of(new Run());
        when(runRepository.findByVisibilityOrderByCreatedOnDesc(RunVisibility.PUBLIC)).thenReturn(runs);

        List<Run> result = runService.getVisibleRuns(null);

        assertEquals(runs, result);
    }

    @Test
    void getVisibleRuns_ShouldReturnVisibleRunsForUser() {
        User user = new User();
        List<Run> runs = List.of(new Run());

        when(runRepository.findVisibleRunsForUser(user)).thenReturn(runs);

        List<Run> result = runService.getVisibleRuns(user);

        assertEquals(runs, result);
    }

    @Test
    void getFeed_ShouldReturnMappedDtoList() {
        User current = new User();
        current.setId(UUID.randomUUID());

        User other = new User();
        other.setId(UUID.randomUUID());
        other.setUsername("TestUser");

        Run run = new Run();
        run.setId(UUID.randomUUID());
        run.setUser(other);
        run.setDistance(5.0);
        run.setDuration(Duration.ofMinutes(25));
        run.setPace("5:00");
        run.setTitle("Evening Run");

        when(runRepository.findVisibleRunsForUser(current)).thenReturn(List.of(run));
        when(likeService.getLikesCount(run.getId())).thenReturn(7);
        when(likeService.isRunLikedByUser(current, run)).thenReturn(true);

        List<RunResponseDto> result = runService.getFeed(current);

        assertEquals(1, result.size());
        RunResponseDto dto = result.get(0);

        assertEquals(run.getId(), dto.id());
        assertEquals(other.getId(), dto.userId());
        assertEquals("TestUser", dto.username());
        assertEquals(5.0, dto.distance());
        assertEquals("25:00", dto.duration());
        assertEquals("5:00", dto.pace());
        assertEquals("Evening Run", dto.title());
        assertEquals(7, dto.likesCount());
        assertTrue(dto.likedByCurrentUser());
    }

    @Test
    void getRunsByUser_ShouldReturnRuns() {
        User user = new User();
        List<Run> runs = List.of(new Run());

        when(runRepository.findByUser(user)).thenReturn(runs);

        assertEquals(runs, runService.getRunsByUser(user));
    }

    @Test
    void getRunById_ShouldReturnRun() {
        UUID id = UUID.randomUUID();
        Run run = new Run();
        when(runRepository.findById(id)).thenReturn(Optional.of(run));

        Run result = runService.getRunById(id);

        assertEquals(run, result);
    }

    @Test
    void getRunById_ShouldThrowIfNotFound() {
        UUID id = UUID.randomUUID();
        when(runRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> runService.getRunById(id));
    }
}

