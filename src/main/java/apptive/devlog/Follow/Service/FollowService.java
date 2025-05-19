package apptive.devlog.Follow.Service;

import apptive.devlog.Member.Domain.Member;
import java.util.List;

public interface FollowService {
    List<Member> getFollowers(Member author);
    // 팔로우/언팔로우 등 추가 가능
} 