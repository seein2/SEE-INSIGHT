package com.seein.domain.member.repository;

import com.seein.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 회원 Repository
 */
public interface MemberRepository extends JpaRepository<Member, Integer> {

    /**
     * 이메일로 회원 조회
     */
    Optional<Member> findByEmail(String email);

    /**
     * 이메일 존재 여부 확인
     */
    boolean existsByEmail(String email);

    /**
     * 이메일로 삭제되지 않은 회원 조회
     */
    Optional<Member> findByEmailAndDeletedAtIsNull(String email);
}
