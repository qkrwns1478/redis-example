package com.example.redis;

import com.example.redis.domain.Item;
import com.example.redis.domain.ItemDto;
import com.example.redis.domain.ItemOrder;
import com.example.redis.domain.ItemOrderDto;
import com.example.redis.repo.ItemRepository;
import com.example.redis.repo.OrderRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class ItemService {
    private final ItemRepository itemRepository;
    private final OrderRepository orderRepository;
    private final ZSetOperations<String, ItemDto> rankOps;
    private final RedisTemplate<String, ItemOrderDto> orderTemplate;
    private final ListOperations<String, ItemOrderDto> orderOps;

    public ItemService(
            ItemRepository itemRepository,
            OrderRepository orderRepository,
            RedisTemplate<String, ItemDto> rankTemplate,
            RedisTemplate<String, ItemOrderDto> orderTemplate
    ) {
        this.itemRepository = itemRepository;
        this.orderRepository = orderRepository;
        this.rankOps = rankTemplate.opsForZSet();
        this.orderTemplate = orderTemplate;
        this.orderOps = this.orderTemplate.opsForList();
    }

    @Cacheable(cacheNames = "itemCache", key = "args[0]")
    public ItemDto readOne(Long id) {
        log.info("Read One: {}", id);
        return itemRepository.findById(id)
                .map(ItemDto::fromEntity)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @Cacheable(cacheNames = "itemAllCache", key = "methodName")
    public List<ItemDto> readAll() {
        return itemRepository.findAll()
                .stream()
                .map(ItemDto::fromEntity)
                .toList();
    }

    @CachePut(cacheNames = "itemCache", key = "#result.id")
    public ItemDto create(ItemDto dto) {
        return ItemDto.fromEntity(itemRepository.save(Item.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .price(dto.getPrice())
                .build()));
    }

    @CachePut(cacheNames = "itemCache", key = "args[0]")
    @CacheEvict(cacheNames = "itemAllCache", allEntries = true)
    public ItemDto update(Long id, ItemDto dto) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        item.setName(dto.getName());
        item.setDescription(dto.getDescription());
        item.setPrice(dto.getPrice());
        return ItemDto.fromEntity(itemRepository.save(item));
    }

    public void delete(Long id) {
        itemRepository.deleteById(id);
    }

    @Cacheable(
            cacheNames = "itemSearchCache",
            key = "{ args[0], args[1].pageNumber, args[1].pageSize }"
    )
    public Page<ItemDto> searchByName(String query, Pageable pageable) {
        return itemRepository.findAllByNameContains(query, pageable)
                .map(ItemDto::fromEntity);
    }

    public void purchase(ItemOrderDto dto) {
        Item item = itemRepository.findById(dto.getItemId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        // DB에 바로 저장하지 않고 "orderCache::behind"라는 키의 리스트 오른쪽에 추가 (RPUSH)
        orderOps.rightPush("orderCache::behind", dto); // Write-Behind
        rankOps.incrementScore(
                "soldRanks",
                ItemDto.fromEntity(item),
                1
        );
    }

    @Transactional
    @Scheduled(fixedRate = 20, timeUnit = TimeUnit.SECONDS)
    public void insertOrders() {
        boolean exists = Optional.ofNullable(orderTemplate.hasKey("orderCache::behind"))
                .orElse(false);
        if (!exists) {
            log.info("no orders in cache");
            return;
        }

        // 처리할 데이터를 orderCache::now로 옮겨서 처리 도중 들어올 새 주문과 분리함
        orderTemplate.rename("orderCache::behind", "orderCache::now");

        // DB 일괄 저장 (Batch Insert)
        log.info("saving {} orders to db", orderOps.size("orderCache::now"));
        orderRepository.saveAll(orderOps.range("orderCache::now", 0, -1).stream()
                .map(dto -> ItemOrder.builder()
                        .itemId(dto.getItemId())
                        .count(dto.getCount())
                        .build())
                .toList());

        // 처리 완료된 캐시 삭제
        orderTemplate.delete("orderCache::now");
    }

    public List<ItemDto> getMostSold() {
        Set<ItemDto> ranks = rankOps.reverseRange("soldRanks", 0, 9);
        if (ranks == null) return Collections.emptyList();
        return ranks.stream().toList();
    }
}
