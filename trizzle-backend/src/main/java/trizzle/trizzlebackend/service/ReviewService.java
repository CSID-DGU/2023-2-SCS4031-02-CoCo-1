package trizzle.trizzlebackend.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Service;
import trizzle.trizzlebackend.Utils.JwtUtil;
import trizzle.trizzlebackend.domain.*;
import trizzle.trizzlebackend.dto.response.ReviewDto;
import trizzle.trizzlebackend.repository.BookmarkRepository;
import trizzle.trizzlebackend.repository.ElasticReviewRepository;
import trizzle.trizzlebackend.repository.LikeRepository;
import trizzle.trizzlebackend.repository.ReviewRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReviewService {

    @Autowired
    private final ElasticReviewRepository elasticReviewRepository;
    private final ReviewRepository reviewRepository;
    private final PlaceService placeService;
    private final BookmarkRepository bookmarkRepository;
    private final LikeRepository likeRepository;
    @Value("${jwt.secret}")
    private String secretKey;

    public Review insertReview(Review review, String accountId) {
        review.setAccountId(accountId);
        LocalDateTime dateTime = LocalDateTime.now();
        review.setReviewRegistrationDate(dateTime);   // 일정 등록 시 현재시간을 등록시간으로 저장

        Place place = review.getPlace();
        Optional<Place> existingPlace = placeService.findByPlaceId(place.getId());
        if (!existingPlace.isPresent()) {    // place정보가 db에 없다면 저장
            placeService.savePlace(place);
        }
        Review insert = reviewRepository.save(review);
        ElasticReview elasticReview = new ElasticReview();
        elasticReview.setData(insert.getId(), insert.getAccountId(), insert.getReviewTitle(), insert.getReviewRegistrationDate(),
                insert.getVisitDate(), insert.getPlace(), insert.getReviewContent(), insert.getPlanId(), insert.getPostId(),
                insert.getPostName(), insert.getThumbnail(), insert.isReviewSecret(), insert.getLikeCount(), insert.getBookmarkCount());
        elasticReviewRepository.save(elasticReview);

        return insert;
    }

    public ReviewDto searchReview(String reviewId, HttpServletRequest request) {
        Optional<Review> reviewOptional = reviewRepository.findById((reviewId));
        if (reviewOptional.isPresent()) {   // reviewId에 해당하는 review가 있을 경우
            Review review = reviewOptional.get();
            ReviewDto reviewDto = new ReviewDto();
            reviewDto.setReview(review);

            if (review.isReviewSecret()) {  // 비공개일 경우 cookie의 accountId와 review의 accountId 비교
                String token = JwtUtil.getAccessTokenFromCookie(request);
                String accountId;
                if (token == null) {    // token없는 경우 null반환
                    return null;
                } else{
                    accountId = JwtUtil.getAccountId(token,secretKey);
                }
                
                if (accountId.equals(review.getAccountId())) {     //cookie의 accountId와 review의 accountId 일치하는 경우
                    return reviewDto;
                } else return null;
                
            } else { // 공개 review일 경우 review 반환
                String token = JwtUtil.getAccessTokenFromCookie(request);
                String accountId;
                if (token == null) {
                    return reviewDto;
                } else {
                    accountId = JwtUtil.getAccountId(token, secretKey);
                }
                Like like = likeRepository.findByReviewIdAndAccountId(reviewId, accountId);
                if (like != null) {     // 좋아요 했으면 isLike true로
                    reviewDto.setLike(true);
                } else { // 좋아요 안했으면 isLike false로
                    reviewDto.setLike(false);
                }
                return reviewDto;
            }

        } else {                            // reviewId에 해당하는 review가 없을 경우
            return null;
        }
    }

    public Page<ElasticReview> findAllReview(Pageable pageable) {
        Page<ElasticReview> reviews = elasticReviewRepository.findAll(pageable);
        return reviews;
    }

    public Review findReview(String reviewId) {
        Optional<Review> optionalReview = reviewRepository.findById(reviewId);
        return optionalReview.orElse(null);
    }

    public Review updateReview(Review review, String reviewId, String accountId) {
        review.setId(reviewId);
        return insertReview(review, accountId);
    }

    public List<Review> findMyReviews(String accountId) {
        List<Review> myReviews = reviewRepository.findByAccountId(accountId);
        return myReviews;
    }

    public void deleteReview(String reviewId) {
        elasticReviewRepository.deleteById(reviewId);
        reviewRepository.deleteById(reviewId);
    }

    public List<Review> findBookmarkReviews(String accountId) {
        String type = "review";
        List<Bookmark> bookmarks = bookmarkRepository.findByAccountIdAndType(accountId, type);
        List<Review> reviews = new ArrayList<>();

        for (Bookmark bookmark : bookmarks) {
            Review review = reviewRepository.findById(bookmark.getReviewId()).orElse(null);
            if (review != null) {
                reviews.add(review);
            }
        }

        return reviews;
    }

    public Review checkMyReview(String reviewId, String accountId) {
        return reviewRepository.findByIdAndAccountId(reviewId, accountId);
    }

    public List<Review> findReviewsWithPlaceId(String placeId) {
        Boolean secret = false;
        List<Review> reviews = reviewRepository.findByPlaceIdAndReviewSecret(placeId, false);
        return reviews;
    }

}
