package com.networkthinking.cm;

import com.mongodb.DB;
import com.mongodb.Mongo;
import com.networkthinking.cm.api.Certificate;
import com.networkthinking.cm.cli.CheckExpireCommand;
import com.networkthinking.cm.db.MongoManaged;
import com.networkthinking.cm.health.MongoHealthCheck;
import com.networkthinking.cm.resources.CertificateResource;
import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import java.security.Security;
import java.util.EnumSet;
import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.mongojack.JacksonDBCollection;

public class cmApplication extends Application<cmConfiguration> {

    public static void main(final String[] args) throws Exception {
        new cmApplication().run(args);
    }

    @Override
    public String getName() {
        return "Certificate Manager";
    }

    @Override
    public void initialize(final Bootstrap<cmConfiguration> bootstrap) {
        bootstrap.addCommand(new CheckExpireCommand(this));
    }

    @Override
    public void run(final cmConfiguration configuration,
            final Environment environment) {
        
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

        // Enable CORS headers
        final FilterRegistration.Dynamic cors
                = environment.servlets().addFilter("CORS", CrossOriginFilter.class);

        // Configure CORS parameters
        cors.setInitParameter("allowedOrigins", "*");
        cors.setInitParameter("allowedHeaders", "X-Requested-With,Content-Type,Accept,Origin");
        cors.setInitParameter("allowedMethods", "OPTIONS,GET,PUT,POST,DELETE,HEAD");

        // Add URL mapping
        cors.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), true, "/*");

        Mongo mongo = new Mongo(configuration.mongohost, configuration.mongoport);

        MongoManaged mongoManaged = new MongoManaged(mongo);
        environment.lifecycle().manage(mongoManaged);

        environment.healthChecks().register("MongoDB", new MongoHealthCheck(mongo));

        DB db = mongo.getDB(configuration.mongodb);
        JacksonDBCollection<Certificate, String> certificates = JacksonDBCollection.wrap(db.getCollection("certificates"), Certificate.class, String.class);

        final CertificateResource certificateResource = new CertificateResource(certificates);
        environment.jersey().register(certificateResource);

    }

}
