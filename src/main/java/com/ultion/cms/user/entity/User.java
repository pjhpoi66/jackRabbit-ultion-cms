package com.ultion.cms.user.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.annotation.LastModifiedDate;

import javax.persistence.*;
import java.sql.Date;

@Getter
@Setter
@Entity
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;

    @Builder
    public User(String userId, String pw) {
        this.userId = userId;
        this.pw = pw;
    }

    @Column(unique = true)
    String userId;
    String pw;

    @CreationTimestamp
    Date registerDateTime;
    @LastModifiedDate
    Date lastModified;

}
