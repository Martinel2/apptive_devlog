package apptive.devlog.Global.Mail;

import org.springframework.stereotype.Service;

@Service
public class MailTemplateService {
    public String buildCommentTemplate(String commentContent) {
        return "[블로그] 회원님의 글에 댓글이 달렸습니다: " + commentContent;
    }
    public String buildReplyTemplate(String replyContent) {
        return "[블로그] 회원님의 댓글에 대댓글이 달렸습니다: " + replyContent;
    }
    public String buildFollowPostTemplate(String author, String postTitle) {
        return "[블로그] 팔로우한 " + author + "님이 새 글을 작성했습니다: " + postTitle;
    }
} 