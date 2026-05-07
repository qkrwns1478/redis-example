package com.example.redisdemo;

import org.springframework.data.repository.CrudRepository;

public interface ItemRepository
        extends CrudRepository<Item, String> {}
