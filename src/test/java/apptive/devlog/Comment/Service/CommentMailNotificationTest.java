package apptive.devlog.Comment.Service;

import apptive.devlog.Comment.Domain.Comment;
import apptive.devlog.Comment.Dto.CreateCommentRequest;
import apptive.devlog.Comment.Repository.CommentRepository;
import apptive.devlog.Member.Domain.Member;
import apptive.devlog.Post.Domain.Post;
import apptive.devlog.Post.Repository.PostRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("댓글/대댓글 메일 알림 시스템")
class CommentMailNotificationTest {

    @Mock
    private MailQueueService mailQueueService;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private PostRepository postRepository;
    @InjectMocks
    private CommentServiceImpl commentService;

    @Test
    @DisplayName("글에 댓글이 달리면 작성자에게 메일이 큐에 등록된다")
    void notifyPostAuthorOnComment() {
        // given
        Member postAuthor = new Member("author@email.com", ...);
        Post post = new Post("제목", "내용", postAuthor);
        Member commenter = new Member("commenter@email.com", ...);
        CreateCommentRequest request = new CreateCommentRequest("댓글 내용");

        when(postRepository.findById(anyLong())).thenReturn(Optional.of(post));
        when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        commentService.createComment(1L, request, commenter);

        // then
        verify(mailQueueService).enqueueMail(
            eq(postAuthor.getEmail()),
            contains("댓글이 달렸습니다")
        );
    }

    @Test
    @DisplayName("메일 발송 실패시 3회까지 재시도한다")
    void retryMailOnFailure() {
        // given
        Member postAuthor = new Member("author@email.com", ...);
        Post post = new Post("제목", "내용", postAuthor);
        Member commenter = new Member("commenter@email.com", ...);
        CreateCommentRequest request = new CreateCommentRequest("댓글 내용");

        when(postRepository.findById(anyLong())).thenReturn(Optional.of(post));
        when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        doThrow(new MailSendException("smtp fail"))
            .when(mailQueueService).sendMail(any());

        // when
        commentService.createComment(1L, request, commenter);

        // then
        verify(mailQueueService, times(3)).sendMail(any());
    }

    @Test
    @DisplayName("내 댓글에 대댓글이 달리면 댓글 작성자에게 메일이 전송된다")
    void notifyCommentAuthorOnReply() {
        // given
        Member commentAuthor = new Member("commenter@email.com", ...);
        Comment parentComment = new Comment("부모댓글", commentAuthor, ...);
        Post post = new Post("제목", "내용", ...);
        parentComment.setPost(post);

        Member replier = new Member("replier@email.com", ...);
        CreateCommentRequest replyRequest = new CreateCommentRequest("대댓글", parentComment.getId());

        when(commentRepository.findById(parentComment.getId())).thenReturn(Optional.of(parentComment));
        when(postRepository.findById(anyLong())).thenReturn(Optional.of(post));
        when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        commentService.createComment(post.getId(), replyRequest, replier);

        // then
        verify(mailQueueService).enqueueMail(
            eq(commentAuthor.getEmail()),
            contains("대댓글이 달렸습니다")
        );
    }

    @Test
    @DisplayName("수신거부한 사용자는 메일을 받지 않는다")
    void doNotSendMailToOptedOutUser() {
        // given
        Member postAuthor = new Member("author@email.com", ...);
        postAuthor.setMailOptOut(true); // 수신거부 설정
        Post post = new Post("제목", "내용", postAuthor);
        Member commenter = new Member("commenter@email.com", ...);
        CreateCommentRequest request = new CreateCommentRequest("댓글 내용");

        when(postRepository.findById(anyLong())).thenReturn(Optional.of(post));
        when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        commentService.createComment(1L, request, commenter);

        // then
        verify(mailQueueService, never()).enqueueMail(eq(postAuthor.getEmail()), anyString());
    }
} 