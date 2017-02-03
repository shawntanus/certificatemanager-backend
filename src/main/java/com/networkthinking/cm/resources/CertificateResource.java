/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.networkthinking.cm.resources;

import com.codahale.metrics.annotation.Timed;
import com.mongodb.MongoException;
import com.networkthinking.cm.api.Certificate;
import com.networkthinking.cm.core.CertificateUtils;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import org.mongojack.DBCursor;
import org.mongojack.DBQuery;
import org.mongojack.JacksonDBCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Shawn
 */
@Path("/api/certificate")
@Produces(MediaType.APPLICATION_JSON)
public class CertificateResource {

    static Logger logger = LoggerFactory.getLogger(CertificateResource.class);
    private final JacksonDBCollection<Certificate, String> collection;

    public CertificateResource(JacksonDBCollection<Certificate, String> certificates) {
        this.collection = certificates;
    }

    @GET
    @Produces(value = MediaType.APPLICATION_JSON)
    @Timed
    @Path("/list")
    public List<Certificate> query() {
        DBCursor<Certificate> dbCursor = collection.find();
        List<Certificate> certificates = new ArrayList<>();
        while (dbCursor.hasNext()) {
            certificates.add(dbCursor.next());
        }
        return certificates;
    }

    @GET
    @Produces(value = MediaType.APPLICATION_JSON)
    @Timed
    @Path("/query/{serverName}")
    public Certificate query(@PathParam("serverName") String serverName) throws Exception {
        return CertificateUtils.getFromServer(serverName);
    }

    @POST
    @Timed
    @Path("/add/{serverName}")
    public Response add(@PathParam("serverName") String serverName) throws Exception {
        logger.info("Adding server: {}", serverName);
        Certificate newCert = CertificateUtils.getFromServer(serverName);
        logger.info("Got CommonName: {}", newCert.getCommonName());
        Certificate certificate = collection.findOne(DBQuery.is("publicKey", newCert.getPublicKey()));
        if (null == certificate) {
            collection.insert(newCert);
            return Response.noContent().build();
        } else {
            throw new WebApplicationException("This certificate is already imported", Response.Status.NOT_ACCEPTABLE);
        }

    }

    @POST
    @Timed
    @Path("/delete/{id}")
    public Response delete(@PathParam("id") String id) {
        Certificate certificate = findCertificate(id);
        logger.error("Removing certificate CommonName:{} Server:{}", certificate.getCommonName(), certificate.getServer());
        collection.removeById(id);
        return Response.noContent().build();
    }

    @POST
    @Timed
    @Produces(value = MediaType.APPLICATION_JSON)
    @Consumes("text/plain")
    @Path("/updatePrivatekey/{id}")
    public Response updatePrivatekey(@PathParam("id") String id, String privateKey) throws Exception {
        Certificate certificate = findCertificate(id);

        if (CertificateUtils.match(certificate.getPublicKey(), privateKey)) {
            certificate.setPrivateKey(privateKey);
            collection.save(certificate);
            logger.info("PrivateKey matches");
            return Response.noContent().build();
        } else {
            logger.info("PrivateKey doesn't match");
            return Response.status(Response.Status.NOT_ACCEPTABLE).build();
        }
    }

    @GET
    @Timed
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @Path("/export/nginx/{id}")
    public Response exportNginx(@PathParam("id") String id) {
        Certificate certificate = findCertificateWithKey(id);

        StreamingOutput soo = (OutputStream out) -> {
            ZipOutputStream zos = new ZipOutputStream(out);
            zos.putNextEntry(new ZipEntry("privateKey.pem"));
            zos.write(certificate.getPrivateKey().getBytes());
            zos.putNextEntry(new ZipEntry("certificate.pem"));
            zos.write(certificate.getPublicKey().getBytes());
            zos.write(certificate.getCaChain().getBytes());
            zos.finish();
            zos.close();
        };

        return Response.ok(soo).header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"certificate.zip\"").build();
    }

    @GET
    @Timed
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @Path("/export/apache/{id}")
    public Response exportApache(@PathParam("id") String id) {
        Certificate certificate = findCertificateWithKey(id);

        StreamingOutput soo = (OutputStream out) -> {
            ZipOutputStream zos = new ZipOutputStream(out);
            zos.putNextEntry(new ZipEntry("privateKey.pem"));
            zos.write(certificate.getPrivateKey().getBytes());
            zos.putNextEntry(new ZipEntry("certificate.pem"));
            zos.write(certificate.getPublicKey().getBytes());
            zos.putNextEntry(new ZipEntry("ca.pem"));
            zos.write(certificate.getCaChain().getBytes());
            zos.finish();
            zos.close();
        };

        return Response.ok(soo).header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"certificate.zip\"").build();
    }

    @GET
    @Timed
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @Path("/export/IIS/{id}")
    public Response exportIIS(@PathParam("id") String id) throws Exception {
        Certificate certificate = findCertificateWithKey(id);

        byte[] pkcs12 = CertificateUtils.generatePKCS12(certificate.getPrivateKey(), certificate.getPublicKey());

        return Response.ok(pkcs12).header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"certificate.p12\"").build();
    }

    private Certificate findCertificateWithKey(String id) throws MongoException, WebApplicationException {
        Certificate certificate = findCertificate(id);
        if(certificate.getPrivateKey()==null){
            throw new WebApplicationException("This certificate doesn't have a private key", Response.Status.NOT_ACCEPTABLE);
        }
        return certificate;
    }

    private Certificate findCertificate(String id) throws WebApplicationException, MongoException {
        Certificate certificate = collection.findOneById(id);
        if (certificate == null) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        return certificate;
    }
}
