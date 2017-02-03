/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.networkthinking.cm.core;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.xml.bind.DatatypeConverter;
import org.bouncycastle.util.io.pem.PemObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.security.provider.X509Factory;
import sun.security.x509.X500Name;
import org.bouncycastle.util.io.pem.PemReader;

/**
 *
 * @author Shawn
 */
public class CertificateUtils {

    static Logger logger = LoggerFactory.getLogger(CertificateUtils.class);

    public static com.networkthinking.cm.api.Certificate getFromServer(String server) throws NoSuchAlgorithmException, KeyManagementException, MalformedURLException, IOException, CertificateEncodingException {

        URL url = new URL("https://" + server);

        SSLContext sslCtx = SSLContext.getInstance("TLS");
        sslCtx.init(null, new TrustManager[]{new X509TrustManager() {

            private X509Certificate[] accepted;

            @Override
            public void checkClientTrusted(X509Certificate[] xcs, String string) throws CertificateException {
            }

            @Override
            public void checkServerTrusted(X509Certificate[] xcs, String string) throws CertificateException {
                accepted = xcs;
            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return accepted;
            }
        }}, null);

        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();

        connection.setHostnameVerifier((String string, SSLSession ssls) -> true);

        connection.setSSLSocketFactory(sslCtx.getSocketFactory());
        connection.connect();

        Certificate[] certificates = connection.getServerCertificates();

        com.networkthinking.cm.api.Certificate certificate = parseCertificates(certificates);

        certificate.setServer(server);
        return certificate;
    }

    private static com.networkthinking.cm.api.Certificate parseCertificates(Certificate[] certs) throws IOException, CertificateEncodingException {
        String commonName;
        Date expirationDate;
        StringBuilder publicKey = new StringBuilder();
        StringBuilder caChain = new StringBuilder();
        Base64.Encoder encoder = Base64.getEncoder();
        com.networkthinking.cm.api.Certificate parsedCertificate = new com.networkthinking.cm.api.Certificate();

        Certificate cert = certs[0];
        commonName = X500Name.asX500Name(((X509Certificate) cert).getSubjectX500Principal()).getCommonName();
        logger.info("Parsed CommonName: {}", commonName);

        publicKey.append(X509Factory.BEGIN_CERT);
        publicKey.append("\n");
        publicKey.append(DatatypeConverter.printBase64Binary(cert.getEncoded()).replaceAll("(.{64})", "$1\n"));
        publicKey.append("\n");
        publicKey.append(X509Factory.END_CERT);
        expirationDate = ((X509Certificate) cert).getNotAfter();

        for (int i = 1; i < certs.length; i++) {
            cert = certs[i];
            caChain.append(X509Factory.BEGIN_CERT);
            caChain.append("\n");
            caChain.append(DatatypeConverter.printBase64Binary(cert.getEncoded()).replaceAll("(.{64})", "$1\n"));
            caChain.append("\n");
            caChain.append(X509Factory.END_CERT);
            caChain.append("\n");
        }

        parsedCertificate.setCommonName(commonName);
        parsedCertificate.setExpirationDate(expirationDate);
        parsedCertificate.setPublicKey(publicKey.toString());
        parsedCertificate.setCaChain(caChain.toString());

        return parsedCertificate;
    }

    public static boolean match(String publicKey, String privateKey) throws CertificateException, IOException, NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException {
        RSAPublicKey rsaPublicKey = getRSAPublicKey(publicKey);
        RSAPrivateKey rsaPrivateKey = getRSAPrivateKey(privateKey);

        return rsaPublicKey.getModulus().equals(rsaPrivateKey.getModulus());
    }

    public static RSAPublicKey getRSAPublicKey(String publicKey) throws CertificateException {
        Certificate certificate = getX509Certificate(publicKey);
        return (RSAPublicKey) certificate.getPublicKey();
    }

    public static Certificate getX509Certificate(String publicKey) throws CertificateException {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        Certificate certificate = CertificateFactory.getInstance("X.509").generateCertificate(new ByteArrayInputStream(publicKey.getBytes()));
        return certificate;
    }

    public static RSAPrivateKey getRSAPrivateKey(String privateKey) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchProviderException {
        PemReader reader = new PemReader(new StringReader(privateKey));
        PemObject pemObject = reader.readPemObject();
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(pemObject.getContent());
        KeyFactory kf = KeyFactory.getInstance("RSA", "BC");
        return (RSAPrivateKey) kf.generatePrivate(keySpec);
    }

    public static byte[] generatePKCS12(String privateKey, String publicKey) throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException, InvalidKeySpecException, NoSuchProviderException {
        Certificate certificate = getX509Certificate(publicKey);
        RSAPrivateKey rsaPrivateKey = getRSAPrivateKey(privateKey);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        String password = "Marvin2000";
        KeyStore ks = KeyStore.getInstance("PKCS12");
        ks.load(null);
        ks.setKeyEntry("alias", (Key) rsaPrivateKey, password.toCharArray(), new java.security.cert.Certificate[]{(java.security.cert.Certificate)certificate});
        ks.store(bos, password.toCharArray());
        bos.close();
        
        return bos.toByteArray();
    }

}
