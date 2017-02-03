/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.networkthinking.cm.health;

import com.codahale.metrics.health.HealthCheck;
import com.mongodb.Mongo;
import java.util.List;

/**
 *
 * @author Shawn
 */
public class MongoHealthCheck extends HealthCheck {
 
    private final Mongo mongo;
 
    public MongoHealthCheck(Mongo mongo) {
        this.mongo = mongo;
    }
 
    @Override
    protected Result check() throws Exception {
        List<String> result = mongo.getDatabaseNames();
        if(result.size()>0){
            return Result.healthy();
        }else{
            return Result.unhealthy("No DB found");
        }
    }
 
}