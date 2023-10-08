package study.datajpa.entity;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
@Getter@Setter
public class Member {

    @Id @GeneratedValue
    private Long id;
    private String username;

    protected Member(){} // JPA 표준스팩에서 디폴트생성자가 필요

    public Member(String username) {
        this.username = username;
    }
}
