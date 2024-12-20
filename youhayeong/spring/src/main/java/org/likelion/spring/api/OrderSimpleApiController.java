package org.likelion.spring.api;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.likelion.spring.domain.Address;
import org.likelion.spring.domain.Order;
import org.likelion.spring.domain.OrderStatus;
import org.likelion.spring.repository.OrderRepository;
import org.likelion.spring.repository.OrderSearch;
import org.likelion.spring.repository.order.simplequery.OrderSimpleQueryDto;
import org.likelion.spring.repository.order.simplequery.OrderSimpleQueryRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

import static java.util.stream.Collectors.toList;

@RestController
@RequiredArgsConstructor
public class OrderSimpleApiController {
    private final OrderRepository orderRepository;
    private final OrderSimpleQueryRepository orderSimpleQueryRepository;

    // V1. 엔티티 직접 노출
    // - Hibernate5Module 모듈 등록, LAZY = null 처리
    // 양방향 관계 문제 발생 -> @JsonIgnore
    @GetMapping("/api/v1/simple-orders")
    public List<Order> ordersV1() {
        List<Order> all = orderRepository.findAllByString(new OrderSearch());
        for (Order order : all) {
            order.getMember().getName(); //Lazy 강제 초기화
            order.getDelivery().getAddress(); //Lazy 강제 초기화
        }
        return all;
    }

    // V2. 엔티티를 조회해서 DTO로 변환(FETCH JOIN 사용 X)
    // 단점: 지연로딩으로 쿼리 N번 호출
    @GetMapping("/api/v2/simple-orders")
    public List<SimpleOrderDto> ordersV2() {
        List<Order> orders = orderRepository.findAll();
        List<SimpleOrderDto> result = orders.stream()
                .map(o -> new SimpleOrderDto(o))
                .collect(toList());

        return result;
    }

    // V3. 엔티티를 조회해서 DTO로 변환(FETCH JOIN 사용 O)
    // FETCH JOIN으로 쿼리 1번 호출
    @GetMapping("/api/v3/simple-orders")
    public List<SimpleOrderDto> ordersV3() {
        List<Order> orders = orderRepository.findAllWithMemberDelivery();
        List<SimpleOrderDto> result = orders.stream()
                .map(o -> new SimpleOrderDto(o))
                .collect(toList());
        return result;
    }

    // V4. JPA 에서 바로 엔티티 꺼내기
    @GetMapping("/api/v4/simple-orders")
    public List<OrderSimpleQueryDto> ordersV4() {
        return orderSimpleQueryRepository.findOrderDtos();
    }

    @Data
    static class SimpleOrderDto {

        private Long orderId;
        private String name;
        private LocalDateTime orderDate; //주문시간
        private OrderStatus orderStatus;
        private Address address;

        public SimpleOrderDto(Order order) {
            orderId = order.getId();
            name = order.getMember().getName();
            orderDate = order.getOrderDate();
            orderStatus = order.getStatus();
            address = order.getDelivery().getAddress();
        }
    }

}
