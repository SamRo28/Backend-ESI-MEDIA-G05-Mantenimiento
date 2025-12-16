package com.example.usersbe.dao;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.example.usersbe.model.LogEntry;

@Repository
public interface LogEntryRepository extends MongoRepository<LogEntry, String> {
    
}