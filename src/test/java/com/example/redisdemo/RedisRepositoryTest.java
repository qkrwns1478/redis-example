package com.example.redisdemo;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class RedisRepositoryTest {
    @Autowired
    private ItemRepository itemRepository;

    @Test
    public void createTest() {
        Item item = Item.builder()
                .name("키보드")
                .description("비싸")
                .price(100000)
                .build();
        itemRepository.save(item);
    }

    @Test @Disabled
    public void createOneTest() {
        Item item = Item.builder()
                .id(String.valueOf(1L))
                .name("마우스")
                .description("너무 비싸")
                .price(50000)
                .build();
        itemRepository.save(item);
    }

    @Test @Disabled
    public void readOneTest() {
        Item item = itemRepository.findById(String.valueOf(1L))
                .orElseThrow();
        System.out.println(item.getDescription());
    }

    @Test @Disabled
    public void updateOneTest() {
        Item item = itemRepository.findById(String.valueOf(1L))
                .orElseThrow();
        item.setDescription("On Sale!!");
        item = itemRepository.save(item);
        System.out.println(item.getDescription());
    }

    @Test @Disabled
    public void deleteOneTest() {
        itemRepository.deleteById(String.valueOf(1L));
    }
}
