package com.EsiMediaG03.dao;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.EsiMediaG03.model.LogEntry;

@Repository
public interface LogEntryRepository extends MongoRepository<LogEntry, String> {
    
}