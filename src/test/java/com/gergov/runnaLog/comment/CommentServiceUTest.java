package com.gergov.runnaLog.comment;

import com.gergov.runnaLog.comment.model.Comment;
import com.gergov.runnaLog.comment.repository.CommentRepository;
import com.gergov.runnaLog.comment.service.CommentService;
import com.gergov.runnaLog.run.model.Run;
import com.gergov.runnaLog.run.repository.RunRepository;
import com.gergov.runnaLog.user.model.User;
import com.gergov.runnaLog.user.model.UserRole;
import com.gergov.runnaLog.web.dto.AddCommentRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceUTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private RunRepository runRepository;

    @InjectMocks
    private CommentService commentService;

    @Test
    void addComment_ShouldSaveComment_WhenRunExists() {
        UUID runId = UUID.randomUUID();
        User user = new User();
        user.setId(UUID.randomUUID());

        Run run = new Run();
        run.setId(runId);

        AddCommentRequest req = new AddCommentRequest();
        req.setContent("Great run!");

        when(runRepository.findById(runId)).thenReturn(Optional.of(run));

        ArgumentCaptor<Comment> captor = ArgumentCaptor.forClass(Comment.class);

        commentService.addComment(runId, req, user);

        verify(commentRepository).save(captor.capture());
        Comment saved = captor.getValue();

        assertEquals("Great run!", saved.getContent());
        assertEquals(user, saved.getUser());
        assertEquals(run, saved.getRun());
        assertNotNull(saved.getCreatedOn());
    }

    @Test
    void addComment_ShouldDoNothing_WhenRunNotFound() {
        UUID runId = UUID.randomUUID();
        User user = new User();
        AddCommentRequest req = new AddCommentRequest();
        req.setContent("Hello");

        when(runRepository.findById(runId)).thenReturn(Optional.empty());

        commentService.addComment(runId, req, user);

        verify(commentRepository, never()).save(any());
    }

    @Test
    void getCommentsForRun_ShouldReturnComments_WhenRunExists() {
        UUID runId = UUID.randomUUID();
        Run run = new Run();
        Comment comment1 = new Comment();
        Comment comment2 = new Comment();

        when(runRepository.findById(runId)).thenReturn(Optional.of(run));
        when(commentRepository.findByRunOrderByCreatedOnAsc(run)).thenReturn(List.of(comment1, comment2));

        List<Comment> comments = commentService.getCommentsForRun(runId);

        assertEquals(2, comments.size());
        assertTrue(comments.contains(comment1));
        assertTrue(comments.contains(comment2));
    }

    @Test
    void getCommentsForRun_ShouldReturnNull_WhenRunNotFound() {
        UUID runId = UUID.randomUUID();

        when(runRepository.findById(runId)).thenReturn(Optional.empty());

        List<Comment> comments = commentService.getCommentsForRun(runId);

        assertNull(comments);
    }

    @Test
    void deleteComment_ShouldDelete_WhenUserIsCommentOwner() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setRole(UserRole.USER);

        Run run = new Run();
        run.setUser(user);
        run.setId(UUID.randomUUID());

        Comment comment = new Comment();
        comment.setUser(user);
        comment.setId(UUID.randomUUID());

        commentService.deleteComment(user, run, comment);

        verify(commentRepository).delete(comment);
    }

    @Test
    void deleteComment_ShouldDelete_WhenUserIsRunOwner() {
        User runOwner = new User();
        runOwner.setId(UUID.randomUUID());
        runOwner.setRole(UserRole.USER);

        User commenter = new User();
        commenter.setId(UUID.randomUUID());

        Run run = new Run();
        run.setUser(runOwner);
        run.setId(UUID.randomUUID());

        Comment comment = new Comment();
        comment.setUser(commenter);
        comment.setId(UUID.randomUUID());

        commentService.deleteComment(runOwner, run, comment);

        verify(commentRepository).delete(comment);
    }

    @Test
    void deleteComment_ShouldDelete_WhenUserIsAdmin() {
        User admin = new User();
        admin.setId(UUID.randomUUID());
        admin.setRole(UserRole.ADMIN);

        User commenter = new User();
        commenter.setId(UUID.randomUUID());

        Run run = new Run();
        run.setUser(commenter);
        run.setId(UUID.randomUUID());

        Comment comment = new Comment();
        comment.setUser(commenter);
        comment.setId(UUID.randomUUID());

        commentService.deleteComment(admin, run, comment);

        verify(commentRepository).delete(comment);
    }

    @Test
    void deleteComment_ShouldThrow_WhenUserNotAllowed() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setRole(UserRole.USER);

        User commenter = new User();
        commenter.setId(UUID.randomUUID());

        User runOwner = new User();
        runOwner.setId(UUID.randomUUID());

        Run run = new Run();
        run.setUser(runOwner);
        run.setId(UUID.randomUUID());

        Comment comment = new Comment();
        comment.setUser(commenter);
        comment.setId(UUID.randomUUID());

        assertThrows(AccessDeniedException.class,
                () -> commentService.deleteComment(user, run, comment));

        verify(commentRepository, never()).delete(any());
    }

    @Test
    void getCommentById_ShouldReturnComment_WhenExists() {
        UUID commentId = UUID.randomUUID();
        Comment comment = new Comment();

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

        Comment result = commentService.getCommentById(commentId);

        assertEquals(comment, result);
    }

    @Test
    void getCommentById_ShouldReturnNull_WhenNotFound() {
        UUID commentId = UUID.randomUUID();

        when(commentRepository.findById(commentId)).thenReturn(Optional.empty());

        Comment result = commentService.getCommentById(commentId);

        assertNull(result);
    }
}

