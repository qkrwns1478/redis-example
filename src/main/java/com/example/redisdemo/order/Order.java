package com.example.redisdemo.order;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@RedisHash("order")
public class Order implements Serializable {
    @Id private String id;
    private String item;
    private Integer count;
    private Long totalPrice;
    private String status;
}
