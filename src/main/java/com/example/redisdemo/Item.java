package com.example.redisdemo;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@RedisHash("item") // Entity 대신 사용
public class Item implements Serializable {
    @Id private String id; // Id를 String으로 쓰면 UUID가 자동으로 배정됨
    private String name;
    private String description;
    private Integer price;
}
