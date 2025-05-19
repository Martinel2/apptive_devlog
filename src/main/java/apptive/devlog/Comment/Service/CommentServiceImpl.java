package apptive.devlog.Comment.Service;

import apptive.devlog.Global.Exception.*;
import apptive.devlog.Member.Domain.Member;
import apptive.devlog.Post.Domain.Post;
import apptive.devlog.Post.Repository.PostRepository;
import apptive.devlog.Comment.Domain.Comment;
import apptive.devlog.Comment.Dto.CreateCommentRequest;
import apptive.devlog.Comment.Repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import apptive.devlog.Global.Mail.MailQueueService;
import apptive.devlog.Global.Mail.MailTemplateService;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final MailQueueService mailQueueService;
    private final MailTemplateService mailTemplateService;

    @Override
    @Transactional
    public Long createComment(Long postId, CreateCommentRequest request, Member member) {
        validateCommentContent(request);

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException());

        Comment comment = new Comment();
        comment.setContent(request.getContent());
        comment.setAuthor(member);
        comment.setPost(post);

        // 대댓글인 경우
        if (request.getParentId() != null) {
            Comment parentComment = commentRepository.findById(request.getParentId())
                    .orElseThrow(() -> new CommentNotFoundException());
            
            // 같은 게시글의 댓글인지 확인
            if (!parentComment.getPost().getId().equals(postId)) {
                throw new CommentNotFoundException();
            }
            
            parentComment.addChild(comment);
            // 대댓글 알림
            Member parentAuthor = parentComment.getAuthor();
            if (!parentAuthor.isMailOptOut()) {
                String mailContent = mailTemplateService.buildReplyTemplate(request.getContent());
                mailQueueService.enqueueMail(parentAuthor.getEmail(), mailContent);
            }
        } else {
            // 댓글 알림
            Member postAuthor = post.getAuthor();
            if (!postAuthor.isMailOptOut()) {
                String mailContent = mailTemplateService.buildCommentTemplate(request.getContent());
                mailQueueService.enqueueMail(postAuthor.getEmail(), mailContent);
            }
        }

        Comment savedComment = commentRepository.save(comment);
        return savedComment.getId();
    }

    @Override
    @Transactional
    public void deleteComment(Long postId, Long commentId, Member member) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException());

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException());

        // 같은 게시글의 댓글인지 확인
        if (!comment.getPost().getId().equals(postId)) {
            throw new CommentNotFoundException();
        }

        if (!comment.getAuthor().equals(member)) {
            throw new CommentNoPermissionException();
        }

        // 대댓글이 있는 경우 내용만 삭제하고 댓글은 유지
        if (!comment.getChildren().isEmpty()) {
            comment.setContent("삭제된 댓글입니다.");
        } else {
            commentRepository.delete(comment);
        }
    }

    private void validateCommentContent(CreateCommentRequest request) {
        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            throw new CommentContentBlankException();
        }
    }
} 