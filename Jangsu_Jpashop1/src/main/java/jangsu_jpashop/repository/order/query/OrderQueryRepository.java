package jangsu_jpashop.repository.order.query;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@Repository
@RequiredArgsConstructor
public class OrderQueryRepository {
    private final EntityManager em;

// V4, 컬렉션은 별도로 조회, Query 루트 1회, 컬렉션 N 번
// 단건 조회에서 많이 사용하는 방식
    public List<OrderQueryDto> findOrderQueryDtos() {

        // 루프를 돌면서 컬렉션 추가, 추가 쿼리 실행
        List<OrderQueryDto> result = findOrders();

        result.forEach(o -> {
            List<OrderItemQueryDto> orderItems = findOrderItems(o.getOrderId());
            o.setOrderItems(orderItems);
        });
        return result;
    }

// V4, 1 : N 관계(컬렉션)을 제외한 나머지를 한번에 조회
    private List<OrderQueryDto> findOrders() {
        return em.createQuery("select new jpabook.jangsu_jpashop.api.Dto.OrderQueryDto" +
                        "(o.id, m.name, o.orderDate, o.orderStatus, d.address)" +
                                " from Order o" +
                                " join o.member m" +
                                " join o.delivery d", OrderQueryDto.class)
                .getResultList();
    }

// V4, 1 : N 관계인 orderItems 조회
    private List<OrderItemQueryDto> findOrderItems(Long orderId) {
        return em.createQuery("SELECT new jpabook.jangsu_jpashop.api.Dto.OrderItemQueryDto" +
                "(oi.order.id, i.name, oi.orderPrice, oi.count)" +
                " from OrderItem oi" +
                " join oi.item i" +
                " where oi.order.id = :orderId", OrderItemQueryDto.class)
                .setParameter("orderId", orderId)
                .getResultList();
    }

// V5, 최적화 버전
    public List<OrderQueryDto> findAllByDto_Optimization() {
        // 루트 조회(toOne 코드를 한번에 조회)
        List<OrderQueryDto> result = findOrders();

        // orderItem 컬렉션을 MAP 한방에 조회
        Map<Long, List<OrderItemQueryDto>> orderItemMap = findOrderItemMap(toOrderIds(result));

        // 루프를 돌면서 컴렉션 추가(추가 쿼리 실행 X)
        result.forEach(o -> o.setOrderItems(orderItemMap.get(o.getOrderId())));

        return result;
    }

    private List<Long> toOrderIds(List<OrderQueryDto> result) {
        return result.stream()
                .map(o -> o.getOrderId())
                .collect(toList());
    }


    private Map<Long, List<OrderItemQueryDto>> findOrderItemMap(List<Long> orderIds) {
        List<OrderItemQueryDto> orderItems = em.createQuery(
                "SELECT new jpabook.jangsu_jpashop.api.Dto.OrderItemQueryDto" +
                        "(oi.order.id, i.name, oi.orderPrice, oi.count)" +
                        " From OrderItem oi" +
                        " JOIN oi.item i" +
                        " WHERE oi.order.id in :orderIds", OrderItemQueryDto.class)
                .setParameter("orderIds", orderIds)
                .getResultList();

        return orderItems.stream()
                .collect(Collectors.groupingBy(OrderItemQueryDto::getOrderId));
    }

// V6, JPA에서 DTO로 직접 조회, 플랫 데이터 최적화
public List<OrderFlatDto> findAllByDto_flat() {
    return em.createQuery(
                    "select new jpabook.jangsu_jpashop.api.Dto.OrderFlatDto" +
                            "(o.id, m.name, o.orderDate, o.orderStatus, d.address, i.name, oi.orderPrice, oi.count)" +
                            " from Order o" +
                            " join o.member m" +
                            " join o.delivery d" +
                            " join o.orderItems oi" +
                            " join oi.item i", OrderFlatDto.class)
            .getResultList();
}

}
