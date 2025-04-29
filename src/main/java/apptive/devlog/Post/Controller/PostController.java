package apptive.devlog.Post.Controller;

import apptive.devlog.Global.Response.Result.ResultCode;
import apptive.devlog.Global.Response.Result.ResultResponse;
import apptive.devlog.Member.Domain.Member;
import apptive.devlog.Post.Dto.CreatePostRequest;
import apptive.devlog.Post.Dto.UpdatePostRequest;
import apptive.devlog.Post.Service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;

    @PostMapping
    public ResponseEntity<?> createPost(
            @RequestBody CreatePostRequest request,
            @AuthenticationPrincipal Member member) {
        Long postId = postService.createPost(request, member);
        final ResultResponse response = new ResultResponse(ResultCode.POST_SUCCESS,new PostIdResponse(postId));
        return new ResponseEntity<>(response,response.getStatus());
    }

    @GetMapping("/{postId}")
    public ResponseEntity<?> getPost(@PathVariable Long postId) {
        final ResultResponse response = new ResultResponse(ResultCode.POST_READ_SUCCESS, new PostIdResponse(postId));
        return new ResponseEntity<>(response,response.getStatus());
    }

    @PutMapping("/{postId}")
    public ResponseEntity<?> updatePost(
            @PathVariable Long postId,
            @RequestBody UpdatePostRequest request,
            @AuthenticationPrincipal Member member) {
        postService.updatePost(postId, request, member);
        final ResultResponse response = new ResultResponse(ResultCode.POST_UPDATE_SUCCESS, null);
        return new ResponseEntity<>(response,response.getStatus());
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<?> deletePost(
            @PathVariable Long postId,
            @AuthenticationPrincipal Member member) {
        postService.deletePost(postId, member);
        final ResultResponse response = new ResultResponse(ResultCode.POST_DELETE_SUCCESS, null);
        return new ResponseEntity<>(response,response.getStatus());
    }

    private record PostIdResponse(Long postId) {}
}