package com.gergov.runnaLog.like;

import com.gergov.runnaLog.like.model.Like;
import com.gergov.runnaLog.like.repository.LikeRepository;
import com.gergov.runnaLog.like.service.LikeService;
import com.gergov.runnaLog.run.model.Run;
import com.gergov.runnaLog.run.repository.RunRepository;
import com.gergov.runnaLog.user.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LikeServiceUTest {

    @Mock
    private LikeRepository likeRepository;

    @Mock
    private RunRepository runRepository;

    @InjectMocks
    private LikeService likeService;

    @Test
    void likeRun_ShouldSaveLike_WhenNotAlreadyLiked() {
        UUID runId = UUID.randomUUID();
        User user = new User();
        Run run = new Run();

        when(runRepository.findById(runId)).thenReturn(Optional.of(run));
        when(likeRepository.existsByUserAndRun(user, run)).thenReturn(false);

        ArgumentCaptor<Like> captor = ArgumentCaptor.forClass(Like.class);

        likeService.likeRun(runId, user);

        verify(likeRepository).save(captor.capture());
        Like saved = captor.getValue();

        assertEquals(user, saved.getUser());
        assertEquals(run, saved.getRun());
        assertNotNull(saved.getCreatedOn());
    }

    @Test
    void likeRun_ShouldDoNothing_WhenAlreadyLiked() {
        UUID runId = UUID.randomUUID();
        User user = new User();
        Run run = new Run();

        when(runRepository.findById(runId)).thenReturn(Optional.of(run));
        when(likeRepository.existsByUserAndRun(user, run)).thenReturn(true);

        likeService.likeRun(runId, user);

        verify(likeRepository, never()).save(any());
    }

    @Test
    void likeRun_ShouldDoNothing_WhenRunNotFound() {
        UUID runId = UUID.randomUUID();
        User user = new User();

        when(runRepository.findById(runId)).thenReturn(Optional.empty());

        likeService.likeRun(runId, user);

        verify(likeRepository, never()).save(any());
    }

    @Test
    void unlikeRun_ShouldDeleteLike_WhenExists() {
        UUID runId = UUID.randomUUID();
        User user = new User();
        Run run = new Run();

        when(runRepository.findById(runId)).thenReturn(Optional.of(run));
        when(likeRepository.existsByUserAndRun(user, run)).thenReturn(true);

        likeService.unlikeRun(runId, user);

        verify(likeRepository).deleteByUserAndRun(user, run);
    }

    @Test
    void unlikeRun_ShouldDoNothing_WhenNotLiked() {
        UUID runId = UUID.randomUUID();
        User user = new User();
        Run run = new Run();

        when(runRepository.findById(runId)).thenReturn(Optional.of(run));
        when(likeRepository.existsByUserAndRun(user, run)).thenReturn(false);

        likeService.unlikeRun(runId, user);

        verify(likeRepository, never()).deleteByUserAndRun(any(), any());
    }

    @Test
    void unlikeRun_ShouldDoNothing_WhenRunNotFound() {
        UUID runId = UUID.randomUUID();
        User user = new User();

        when(runRepository.findById(runId)).thenReturn(Optional.empty());

        likeService.unlikeRun(runId, user);

        verify(likeRepository, never()).deleteByUserAndRun(any(), any());
    }

    @Test
    void getLikesCount_ShouldReturnCount_WhenRunExists() {
        UUID runId = UUID.randomUUID();
        Run run = new Run();
        Like like1 = new Like();
        Like like2 = new Like();

        when(runRepository.findById(runId)).thenReturn(Optional.of(run));
        when(likeRepository.findByRun(run)).thenReturn(List.of(like1, like2));

        int count = likeService.getLikesCount(runId);

        assertEquals(2, count);
    }

    @Test
    void getLikesCount_ShouldReturnZero_WhenRunNotFound() {
        UUID runId = UUID.randomUUID();

        when(runRepository.findById(runId)).thenReturn(Optional.empty());

        int count = likeService.getLikesCount(runId);

        assertEquals(0, count);
    }

    @Test
    void isRunLikedByUser_ShouldReturnTrue_WhenExists() {
        User user = new User();
        Run run = new Run();

        when(likeRepository.existsByUserAndRun(user, run)).thenReturn(true);

        assertTrue(likeService.isRunLikedByUser(user, run));
    }

    @Test
    void isRunLikedByUser_ShouldReturnFalse_WhenNotExists() {
        User user = new User();
        Run run = new Run();

        when(likeRepository.existsByUserAndRun(user, run)).thenReturn(false);

        assertFalse(likeService.isRunLikedByUser(user, run));
    }
}

