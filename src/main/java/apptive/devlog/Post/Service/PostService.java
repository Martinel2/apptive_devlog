package apptive.devlog.Post.Service;

import apptive.devlog.Member.Domain.Member;
import apptive.devlog.Post.Domain.Post;
import apptive.devlog.Post.Dto.CreatePostRequest;
import apptive.devlog.Post.Dto.UpdatePostRequest;

public interface PostService {
    Long createPost(CreatePostRequest request, Member member);
    Post getPost(Long postId);
    void updatePost(Long postId, UpdatePostRequest request, Member member);
    void deletePost(Long postId, Member member);
} 