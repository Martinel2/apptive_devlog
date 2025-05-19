package apptive.devlog.Post.Service;

import apptive.devlog.Member.Domain.Member;
import apptive.devlog.Post.Domain.Post;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("팔로우한 사람이 글을 남기면 팔로워에게 메일 알림")
class PostMailNotificationTest {

    @Mock
    private MailQueueService mailQueueService;
    @Mock
    private FollowService followService;
    @InjectMocks
    private PostServiceImpl postService;

    @Test
    @DisplayName("팔로우한 사람이 글을 남기면 팔로워 모두에게 메일이 전송된다")
    void notifyFollowersOnPost() {
        // given
        Member author = new Member("author@email.com", ...);
        List<Member> followers = List.of(
            new Member("follower1@email.com", ...),
            new Member("follower2@email.com", ...),
            new Member("follower3@email.com", ...)
        );
        Post post = new Post("제목", "내용", author);

        when(followService.getFollowers(author)).thenReturn(followers);

        // when
        postService.createPost(post);

        // then
        for (Member follower : followers) {
            verify(mailQueueService).enqueueMail(
                eq(follower.getEmail()),
                contains("새 글이 등록되었습니다")
            );
        }
    }
} 