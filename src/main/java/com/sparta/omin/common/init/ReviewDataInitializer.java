package com.sparta.omin.common.init;

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
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class ReviewDataInitializer implements CommandLineRunner {

    private final UserRepository userRepository; // 패키지 경로 수정됨
    private final RegionRepository regionRepository;
    private final StoreRepository storeRepository;
    private final OrderRepository orderRepository;
    private final PasswordEncoder passwordEncoder;
    private final SeedProperties seedProperties;
    private final ReviewRepository reviewRepository;

    @Override
    @Transactional
    public void run(String... args) {
        if (!seedProperties.isEnabled()) {
            return;
        }

        // 1. 테스트용 지역 생성
        Region region = regionRepository.findAll().stream().findFirst().orElseGet(() -> regionRepository.save(Region.create("서울 강남구 역삼동")));

        // 2. 테스트용 사용자 생성 (Customer)
        User customer = userRepository.findByEmailAndIsDeletedFalse("tester@example.com").orElseGet(() -> {
            User user = User.builder().name("테스터").nickname("리뷰어").email("tester@example.com").password(passwordEncoder.encode("Password123!")).build();
            return userRepository.save(user);
        });

        // 3. 테스트용 사장님 생성 (Store Owner)
        User owner = userRepository.findByEmailAndIsDeletedFalse("owner@example.com").orElseGet(() -> {
            User user = User.builder().name("사장님").nickname("맛집주인").email("owner@example.com").password(passwordEncoder.encode("Password123!")).build();
            return userRepository.save(user);
        });

        // 4. 테스트용 가게 생성 (가게가 없을 때만 생성하도록 권장)
        Store store;
        if (storeRepository.count() == 0) {
            store = Store.builder().ownerId(owner.getId()).regionId(region.getId()).category(Category.CHICKEN).name("테스트 치킨 맛집").roadAddress("서울 강남구 테헤란로").detailAddress("123번지 1층").latitude(new BigDecimal("37.123456")).longitude(new BigDecimal("127.123456")).build();
            store = storeRepository.save(store);
        } else {
            store = storeRepository.findAll().get(0);
        }

        // 5. 테스트용 완료된 주문 생성
        if (orderRepository.count() == 0) {

            Order order1 = Order.createWithId(customer, store);

            orderRepository.save(order1);

            Order order2 = Order.createWithId(customer, store);

            orderRepository.save(order2);

            Order order3 = Order.createWithId(customer, store);

            orderRepository.save(order3);

            System.out.println("TEST ORDER1 = " + order1.getId());
            System.out.println("TEST ORDER2 = " + order2.getId());

            Review review = Review.create(
                    customer,
                    order3,
                    order3.getStore(),
                    3.5,
                    "그냥 그래요!"
            );
            reviewRepository.save(review);
            System.out.println("TEST REVIEW1 = " + review.getId());
        }

    } // run 메서드 종료
}

/*
리뷰 생성 JSON
* {
  "orderId": "ebc6d11e-7723-4e43-81e8-fa4f762246b0",
  "rating": 4,
  "comment": "진짜 최고예요! ㅎㅎ 배달도 빨라요"
}

{
  "orderId": "05a81a10-397f-4c5f-8667-b084ea16deb6",
  "rating": 1.5,
  "comment": "진짜 맛없어요 ㅜㅜ 배달도 느리고 최악입니다"
}
* */