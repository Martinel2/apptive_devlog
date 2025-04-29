package apptive.devlog.Post.Service;

import apptive.devlog.Global.Exception.PostContentBlankException;
import apptive.devlog.Global.Exception.PostNoPermissionException;
import apptive.devlog.Global.Exception.PostNotFoundException;
import apptive.devlog.Global.Exception.PostTitleBlankException;
import apptive.devlog.Member.Domain.Member;
import apptive.devlog.Post.Dto.CreatePostRequest;
import apptive.devlog.Post.Dto.UpdatePostRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;

    @Override
    @Transactional
    public Long createPost(CreatePostRequest request, Member member) {
        validateCreatePostRequest(request);

        Post post = new Post();
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setAuthor(member);

        Post savedPost = postRepository.save(post);
        return savedPost.getId();
    }

    private void validateCreatePostRequest(CreatePostRequest request) {
        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            throw new PostTitleBlankException();
        }
        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            throw new PostContentBlankException();
        }
    }

    @Override
    public Post getPost(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(PostNotFoundException::new);
    }

    @Override
    @Transactional
    public void updatePost(Long postId, UpdatePostRequest request, Member member) {
        Post post = getPost(postId);
        
        if (!post.getAuthor().equals(member)) {
            throw new PostNoPermissionException();
        }

        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            throw new PostTitleBlankException();
        }
        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            throw new PostContentBlankException();
        }

        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
    }

    @Override
    @Transactional
    public void deletePost(Long postId, Member member) {
        Post post = getPost(postId);
        
        if (!post.getAuthor().equals(member)) {
            throw new PostNoPermissionException();
        }

        postRepository.delete(post);
    }
} 