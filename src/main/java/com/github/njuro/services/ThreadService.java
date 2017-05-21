package com.github.njuro.services;

import com.github.njuro.models.Thread;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Service for CRUD operations with threads
 *
 * @author njuro
 */

interface ThreadRepository extends CrudRepository<Thread, Long> {

}

@Service
public class ThreadService {

    private final ThreadRepository threadRepository;

    @Autowired
    public ThreadService(ThreadRepository threadRepository) {
        this.threadRepository = threadRepository;
    }

    public List<Thread> getAllThreads() {
        List<Thread> threads = new ArrayList<>();
        threadRepository.findAll().forEach(threads::add);
        return threads;
    }

}
