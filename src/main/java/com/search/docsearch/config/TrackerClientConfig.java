package com.search.docsearch.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.nio.conn.ssl.SSLIOSessionStrategy;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.TrustStrategy;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.SSLContext;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

@Configuration
@EnableConfigurationProperties()
@Slf4j
public class TrackerClientConfig {
    @Value("${tracker.username}")
    private String trackerUserName;

    @Value("${tracker.password}")
    private String trackerPassword;

    @Value("${tracker.host}")
    private String trackerHost;

    @Value("${tracker.port}")
    private int trackerPort;

    @Bean(destroyMethod = "close")
    public RestHighLevelClient trackerClient() {

        RestHighLevelClient restClient = null;
        try {
            final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(AuthScope.ANY,
                    new UsernamePasswordCredentials(trackerUserName, trackerPassword));
            SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {
                @Override
                public boolean isTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                    return true;
                }
            }).build();
            SSLIOSessionStrategy sessionStrategy = new SSLIOSessionStrategy(sslContext, NoopHostnameVerifier.INSTANCE);
            restClient = new RestHighLevelClient(
                    RestClient.builder(new HttpHost(trackerHost, trackerPort, "https")).setHttpClientConfigCallback(
                            new RestClientBuilder.HttpClientConfigCallback() {
                                @Override
                                public HttpAsyncClientBuilder customizeHttpClient(
                                        HttpAsyncClientBuilder httpAsyncClientBuilder) {
                                    httpAsyncClientBuilder.disableAuthCaching();
                                    httpAsyncClientBuilder.setSSLStrategy(sessionStrategy);
                                    httpAsyncClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
                                    return httpAsyncClientBuilder;
                                }
                            }));
        } catch (Exception e) {
            log.error("elasticsearch TransportClient create error!!", e);
        }
        return restClient;
    }
}
