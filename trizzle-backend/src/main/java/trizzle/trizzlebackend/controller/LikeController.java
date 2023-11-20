package trizzle.trizzlebackend.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import trizzle.trizzlebackend.Utils.JwtUtil;
import trizzle.trizzlebackend.service.LikeService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class LikeController {

    private final LikeService likeService;
    @Value("${jwt.secret}")
    private String secretKey;
    @PostMapping("/{type}/{contentId}/likes")
    public ResponseEntity postLike(@PathVariable("type") String type,
                                   @PathVariable("contentId") String contentId,
                                   HttpServletRequest request) {
        String token = JwtUtil.getAccessTokenFromCookie(request);
        String accountId = JwtUtil.getAccountId(token, secretKey);
        String message = likeService.convertLike(type, contentId, accountId); // 좋아요 추가 : "add like success", 삭제: "delete like success"

        Map<String, String> response = new HashMap<>();
        response.put("message", message);

        return ResponseEntity.ok(response);
    }
}
