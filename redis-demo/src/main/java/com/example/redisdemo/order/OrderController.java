package com.example.redisdemo.order;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("orders")
public class OrderController {
    private final OrderRepository orderRepository;

    public OrderController(
            OrderRepository orderRepository
    ) {
        this.orderRepository = orderRepository;
    }

    @PostMapping
    public Order create(
            @RequestBody
            Order order
    ) {
        return orderRepository.save(order);
    }

    @GetMapping
    public List<Order> readAll() {
        List<Order> orders = new ArrayList<>();
        orderRepository.findAll()
                .forEach(orders::add);
        return orders;
    }

    @GetMapping("{id}")
    public Order readOne(
            @PathVariable("id")
            String id
    ) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @PutMapping("{id}")
    public Order update(
            @PathVariable("id")
            String id,
            @RequestBody
            Order order
    ) {
        Order target = orderRepository
                .findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        target.setItem(order.getItem());
        target.setCount(order.getCount());
        target.setTotalPrice(order.getTotalPrice());
        target.setStatus(order.getStatus());
        return orderRepository.save(target);
    }

    @DeleteMapping("{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable("id")
            String id
    ) {
        orderRepository.deleteById(id);
    }
}
