package com.fe2.service;

import com.fe2.configuration.Configuration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@Service
public class UrlSigner {

    @Autowired
    private Configuration configuration;

    public URL signUrl(URL url) throws InvalidKeyException, NoSuchAlgorithmException, MalformedURLException, URISyntaxException {
        String request = signRequest(url.getPath(), url.getQuery());
        return new URI(url.getProtocol() + "://" + url.getHost() + request).toURL();
    }

    private String signRequest(String path, String query) throws NoSuchAlgorithmException, InvalidKeyException
    {
        // Retrieve the proper URL components to sign
        String resource = path + '?' + query;

        // Get an HMAC-SHA1 signing key from the raw key bytes
        SecretKeySpec sha1Key = new SecretKeySpec(getKeyBinary(), "HmacSHA1");

        // Get an HMAC-SHA1 Mac instance and initialize it with the HMAC-SHA1 key
        Mac mac = Mac.getInstance("HmacSHA1");
        mac.init(sha1Key);

        // compute the binary signature for the request
        byte[] sigBytes = mac.doFinal(resource.getBytes());

        // base 64 encode the binary signature
        String signature = Base64.getEncoder().encodeToString(sigBytes);

        // convert the signature to 'web safe' base 64
        signature = signature.replace('+', '-');
        signature = signature.replace('/', '_');

        return resource + "&signature=" + signature;
    }

    private byte[] getKeyBinary() {
        String keyString = configuration.getGcpMapsSigningKey();
        // Convert the key from 'web safe' base 64 to binary
        keyString = keyString.replace('-', '+');
        keyString = keyString.replace('_', '/');
        return Base64.getDecoder().decode(keyString);
    }
}
