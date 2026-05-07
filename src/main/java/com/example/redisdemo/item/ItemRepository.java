package com.example.redisdemo.item;

import org.springframework.data.repository.CrudRepository;

public interface ItemRepository
        extends CrudRepository<Item, String> {}
