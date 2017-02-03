/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.networkthinking.cm.cli;

import com.mongodb.DB;
import com.mongodb.Mongo;
import com.networkthinking.cm.api.Certificate;
import com.networkthinking.cm.cmConfiguration;
import io.dropwizard.Application;
import io.dropwizard.cli.EnvironmentCommand;
import io.dropwizard.setup.Environment;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import org.mongojack.DBCursor;
import org.mongojack.JacksonDBCollection;

/**
 *
 * @author Shawn
 */
public class CheckExpireCommand extends EnvironmentCommand<cmConfiguration> {

    public CheckExpireCommand(Application<cmConfiguration> service) {
        super(service, "checkExpire", "check expiring certificate");
    }

    @Override
    public void configure(Subparser sbprsr) {
         sbprsr.addArgument("-d", "--days")
                .dest("days")
                .type(int.class)
                .required(false)
                 .setDefault(90)
                .help("Days to be expired");
    }

    @Override
    protected void run(Environment e, Namespace nmspc, cmConfiguration configuration) throws Exception {
        int expiringCount = 0;
        Mongo mongo = new Mongo(configuration.mongohost, configuration.mongoport);

        DB db = mongo.getDB(configuration.mongodb);
        JacksonDBCollection<Certificate, String> certificates = JacksonDBCollection.wrap(db.getCollection("certificates"), Certificate.class, String.class);
        DBCursor<Certificate> cursor = certificates.find();
        while (cursor.hasNext()) {
            Certificate certificte = cursor.next();
            long diff = certificte.getExpirationDate().getTime() - (new Date()).getTime();
            long expiringDays = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
            if (expiringDays <= nmspc.getInt("days")) {
                expiringCount++;
                System.err.println(certificte.getCommonName() + " is expiring in " + expiringDays + " days");
            }
        }
        System.exit(expiringCount);
    }

}
