package study.datajpa.repository;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import study.datajpa.dto.MemberDto;
import study.datajpa.entity.Member;
import study.datajpa.entity.Team;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class MemberRepositoryTest {

    @Autowired MemberRepository memberRepository;
    @Autowired TeamRepository teamRepository;
    @PersistenceContext
    EntityManager em;

    @Test
    public void testMember() throws Exception {
        //given
        Member member = new Member("Lionel");
        //when
        Member savedMember = memberRepository.save(member);
        Member findMember = memberRepository.findById(savedMember.getId()).orElseThrow(() -> {
            return new Exception("없는 유저입니다");
        });
        //then
        assertThat(findMember.getId()).isEqualTo(member.getId());
        assertThat(findMember.getUsername()).isEqualTo(member.getUsername());
        assertThat(findMember).isEqualTo(member);
    }

    @Test
    public void basicCRUD(){
        Member member1 = new Member("member1");
        Member member2 = new Member("member2");

        memberRepository.save(member1);
        memberRepository.save(member2);

        // 단건조회 검증
        Member findMember1 = memberRepository.findById(member1.getId()).get();
        Member findMember2 = memberRepository.findById(member2.getId()).get();
        assertThat(findMember1).isEqualTo(member1);
        assertThat(findMember2).isEqualTo(member2);

        // 리스트 조회
        List<Member> result = memberRepository.findAll();
        assertThat(result.size()).isEqualTo(2);

        // 카운트 검증
        long count = memberRepository.count();
        assertThat(count).isEqualTo(2);

        // 삭제 검증
        memberRepository.delete(member1);
        memberRepository.delete(member2);

        long deleteCount = memberRepository.count();
        assertThat(deleteCount).isEqualTo(0);
    }

    @Test
    public void findByUsernameAndAgeGreaterThen(){
        Member m1 = new Member("aaa", 10);
        Member m2 = new Member("aaa", 20);

        memberRepository.save(m1);
        memberRepository.save(m2);

        List<Member> result = memberRepository.findByUsernameAndAgeGreaterThan("aaa", 15);

        assertThat(result.get(0).getUsername()).isEqualTo("aaa");
        assertThat(result.get(0).getAge()).isEqualTo(20);
        assertThat(result.size()).isEqualTo(1);
    }

    @Test
    void findUsername(){
        Member member = new Member("ronaldo", 7);

        memberRepository.save(member);

        List<Member> result = memberRepository.findByUsername("ronaldo");

        assertThat(result.get(0).getUsername()).isEqualTo("ronaldo");
        assertThat(result.get(0).getAge()).isEqualTo(7);
        assertThat(result.size()).isEqualTo(1);
    }

    @Test
    void testQuery(){
        Member m1 = new Member("aaa", 10);
        Member m2 = new Member("aaa", 20);

        memberRepository.save(m1);
        memberRepository.save(m2);

        List<Member> result = memberRepository.findUser("aaa", 10);
        assertThat(result.get(0)).isEqualTo(m1);
    }

    @Test
    public void testQuery2() throws Exception {
        //given
        Member m1 = new Member("aaa", 10);
        Member m2 = new Member("bbb", 20);
        memberRepository.save(m1);
        memberRepository.save(m2);
        //when
        List<String> usernameList = memberRepository.findUsernameList();
        for (String s : usernameList) {
            System.out.println("s = " + s);
        }
        //then
    }

    @Test
    public void findMemberDto() throws Exception {
        //given
        Team team = new Team("teamA");
        teamRepository.save(team);

        Member m1 = new Member("aaa", 10);
        m1.setTeam(team);
        memberRepository.save(m1);
        //when

        List<MemberDto> memberDto = memberRepository.findMemberDto();
        for (MemberDto dto : memberDto) {
            System.out.println("dto = " + dto);
        }
        //then
    }

    @Test
    public void findByNames() throws Exception {
        //given
        Member m1 = new Member("aaa", 10);
        Member m2 = new Member("bbb", 20);
        memberRepository.save(m1);
        memberRepository.save(m2);
        //when
        List<Member> result = memberRepository.findByNames(Arrays.asList("aaa", "BBB"));
        for (Member member : result) {
            System.out.println("member = " + member);
        }
    }


    @Test
    public void returnType() throws Exception {
        Member m1 = new Member("aaa", 10);
        Member m2 = new Member("bbb", 20);
        memberRepository.save(m1);
        memberRepository.save(m2);

        List<Member> result = memberRepository.findListByUsername("aaa");
        Member result2 = memberRepository.findMemberByUsername("aaa");
        Optional<Member> result3 = memberRepository.findOptionalByUsername("aaa");
    }

    @Test
    public void paging(){
        //given
        memberRepository.save(new Member("member1",10));
        memberRepository.save(new Member("member2",10));
        memberRepository.save(new Member("member3",10));
        memberRepository.save(new Member("member4",10));
        memberRepository.save(new Member("member5",10));
        memberRepository.save(new Member("member6",10));

        int age = 10;
        PageRequest pageRequest = PageRequest.of(0, 3, Sort.by(Sort.Direction.ASC,"username"));

        //when
        Page<Member> page = memberRepository.findByAge(age, pageRequest); // API 통신 시 Entity 자체를 반환하는건 안좋다.

        Page<MemberDto> toMap = page.map(m -> new MemberDto(m.getId(), m.getUsername(), ""));

        //then
        List<Member> content = page.getContent();
        long totalCount = page.getTotalElements();

        assertThat(content.size()).isEqualTo(3);
        assertThat(totalCount).isEqualTo(6);
        assertThat(page.getNumber()).isEqualTo(0);
        assertThat(page.getTotalPages()).isEqualTo(2);
        assertThat(page.isFirst()).isTrue();
        assertThat(page.isLast()).isFalse();
        assertThat(page.hasNext()).isTrue();
    }

    @Test
    public void bulckUpate() throws Exception {
        //given
        memberRepository.save(new Member("member1",10));
        memberRepository.save(new Member("member2",19));
        memberRepository.save(new Member("member3",20));
        memberRepository.save(new Member("member4",21));
        memberRepository.save(new Member("member5",40));
        //when
        int resultCount = memberRepository.bulkAgePlus(20);

        List<Member> findMembers = memberRepository.findByUsername("member5");
        Member member = findMembers.get(0);
        //then
        assertThat(resultCount).isEqualTo(3);

        assertThat(member.getAge()).isEqualTo(41);
    }

    @Test
    public void findMemberLazy() throws Exception {
        //given
        // member1 -> teamA
        // member2 -> teamB
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        teamRepository.save(teamA);
        teamRepository.save(teamB);
        Member member1 = new Member("member1",10,teamA);
        Member member2 = new Member("member2",20,teamB);
        memberRepository.save(member1);
        memberRepository.save(member2);

        em.flush();
        em.clear();

        //when

        /**
         * <li>Lazy 로딩으로 인한 N+1 문제는 패치조인으로 해결가능하다!</li>
         * <li>단, 스프링 데이터 JPA를 쓰면 매번 메서드를 생성해야하는 불편함이 잇다.</li>
         */
        //List<Member> members = memberRepository.findMemberFetchJoin();
        //List<Member> members = memberRepository.findAll();
        List<Member> members = memberRepository.findMemberEntityGraph();
        for (Member member : members) {
            System.out.println("member = " + member.getTeam());
        }
        //then
    }




}