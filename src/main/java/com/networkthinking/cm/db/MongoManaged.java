/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.networkthinking.cm.db;

import com.mongodb.Mongo;
import io.dropwizard.lifecycle.Managed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Shawn
 */
public class MongoManaged implements Managed {
 
    private final Mongo mongo;
    
    Logger logger = LoggerFactory.getLogger(this.getClass());
 
    public MongoManaged(Mongo mongo) {
        this.mongo = mongo;
    }
 
    @Override
    public void start() throws Exception {
        logger.info("MontoManaged started");
    }
 
    @Override
    public void stop() throws Exception {
        mongo.close();
    }
 
}
