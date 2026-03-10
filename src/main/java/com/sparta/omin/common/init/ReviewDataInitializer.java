package com.sparta.omin.common.init;

import com.sparta.omin.app.model.address.entity.Address;
import com.sparta.omin.app.model.address.repos.AddressRepository;
import com.sparta.omin.app.model.order.entity.Order;
import com.sparta.omin.app.model.order.repos.OrderRepository;
import com.sparta.omin.app.model.region.entity.Region;
import com.sparta.omin.app.model.region.repos.RegionRepository;
import com.sparta.omin.app.model.review.entity.Review;
import com.sparta.omin.app.model.review.repos.ReviewRepository;
import com.sparta.omin.app.model.store.code.Category;
import com.sparta.omin.app.model.store.entity.Store;
import com.sparta.omin.app.model.store.repos.StoreRepository;
import com.sparta.omin.app.model.user.entity.User;
import com.sparta.omin.app.model.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ReviewDataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RegionRepository regionRepository;
    private final StoreRepository storeRepository;
    private final OrderRepository orderRepository;
    private final AddressRepository addressRepository;
    private final ReviewRepository reviewRepository;
    private final PasswordEncoder passwordEncoder;
    private final SeedProperties seedProperties;

    // SRID 4326(WGS84)을 사용하는 JTS GeometryFactory
    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    @Override
    @Transactional
    public void run(String... args) {
        if (!seedProperties.isEnabled()) {
            return;
        }

        // 1. 지역(Region) 생성 또는 조회
        Region region = regionRepository.findAll().stream().findFirst()
                .orElseGet(() -> regionRepository.save(Region.create("서울 강남구 역삼동")));

        // 2. 사용자(User) 생성
        User customer = userRepository.findByEmailAndIsDeletedFalse("tester@example.com").orElseGet(() ->
                userRepository.save(User.builder()
                        .name("테스터").nickname("리뷰어")
                        .email("tester@example.com")
                        .password(passwordEncoder.encode("Password123!"))
                        .build())
        );

        User owner = userRepository.findByEmailAndIsDeletedFalse("owner@example.com").orElseGet(() ->
                userRepository.save(User.builder()
                        .name("사장님").nickname("맛집주인")
                        .email("owner@example.com")
                        .password(passwordEncoder.encode("Password123!"))
                        .build())
        );

        // 3. 가게(Store) 생성
        if (storeRepository.count() == 0) {
            saveStore(owner.getId(), "테스트 치킨 맛집", Category.CHICKEN, 37.123456, 127.123456);
            saveStore(owner.getId(), "테스트 짜장 맛집", Category.CHINESE, 37.223456, 127.223456);
        }
        Store store1 = storeRepository.findAll().get(0);
        Store store2 = storeRepository.findAll().get(1);

        // 4. 주소(Address) 생성 (Address.create 메서드 사용)
        Address testAddress = addressRepository.save(Address.create(
                customer.getId(),
                region.getId(),
                "우리집",
                "서울 강남구 테헤란로",
                "4101호",
                new BigDecimal("37.123456"),
                new BigDecimal("127.123456"),
                true
        ));

        // 5. 주문(Order) 및 리뷰(Review) 생성
        if (orderRepository.count() == 0) {
            // 주문 생성 -> COMPLETED 상태로 전환 -> 리뷰 생성 순서로 진행
            createReviewData(customer, store1, testAddress, 5.0, "치킨이 정말 바삭하고 맛있어요!");
            createReviewData(customer, store1, testAddress, 4.0, "맛있는데 배달이 조금 늦었네요.");
            createReviewData(customer, store2, testAddress, 1.5, "음식이 다 식어서 왔어요. 비추합니다.");
        }
    }

    /**
     * 가게 저장 헬퍼 메서드
     */
    private void saveStore(UUID ownerId, String name, Category category, double lat, double lon) {
        Point point = geometryFactory.createPoint(new Coordinate(lon, lat)); // Longitude(X), Latitude(Y)
        Store store = Store.builder()
                .ownerId(ownerId)
                .name(name)
                .category(category)
                .roadAddress("서울 강남구 테헤란로")
                .detailAddress("123번지 1층")
                .coordinates(point)
                .build();
        storeRepository.save(store);
    }

    /**
     * 완료된 주문 생성 및 리뷰 작성 헬퍼 메서드
     */
    private void createReviewData(User user, Store store, Address address, double rating, String comment) {
        // 1. 주문 생성 (초기 상태 PENDING)
        Order order = Order.create(user, store, "조심히 와주세요", address);
        orderRepository.save(order);

        // 2. 상태 전이 (PENDING -> ACCEPTED -> COOKING -> COMPLETED)
        order.nextStatus(); // ACCEPTED
        order.nextStatus(); // COOKING
        order.nextStatus(); // COMPLETED
        orderRepository.save(order);

        // 3. 리뷰 생성
        Review review = Review.create(user, order, store, rating, comment);
        reviewRepository.save(review);
    }
}