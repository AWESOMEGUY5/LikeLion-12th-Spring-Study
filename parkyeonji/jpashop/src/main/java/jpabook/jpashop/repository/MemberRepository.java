package jpabook.jpashop.repository;

import jakarta.persistence.EntityManager;
import jpabook.jpashop.domain.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository // 스프링 빈에 등록
@RequiredArgsConstructor
public class MemberRepository {
    /*@PersistenceContext // 엔티티 매니저 주입 -> @RequiredArgsConstructor */
    private final EntityManager em;

    public void save(Member member) {
        em.persist(member);
    }
    public Member findOne(Long id) {
        return em.find(Member.class, id);
    }
    public List<Member> findAll() {
        return em.createQuery("select m from Member m", Member.class).getResultList();
    }

    public List<Member> findByName(String name) { // 파라미터 name 바인딩 해서 조회
        return em.createQuery("select m from Member m where m.name = :name", Member.class)
                .setParameter("name", name)
                .getResultList();
    }
}

/*public interface MemberRepository extends JpaRepository<Member, Long> {
    List<Member> findByName(String name);
}*/
