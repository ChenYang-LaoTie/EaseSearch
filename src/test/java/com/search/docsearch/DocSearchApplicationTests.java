package com.search.docsearch;

import com.search.docsearch.config.MySystem;
import com.search.docsearch.utils.EulerParse;
import com.search.docsearch.utils.IdUtil;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.nio.conn.ssl.SSLIOSessionStrategy;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.TrustStrategy;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.suggest.Suggest;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.SuggestBuilders;
import org.elasticsearch.search.suggest.SuggestionBuilder;
import org.elasticsearch.search.suggest.term.TermSuggestionBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;

@SpringBootTest
class DocSearchApplicationTests {
	@Autowired
	@Qualifier("restHighLevelClient")
	private RestHighLevelClient restHighLevelClient;

	@Autowired
	@Qualifier("setConfig")
	private MySystem s;



	@Test
	void contextLoads() throws IOException {

		DeleteByQueryRequest deleteByQueryRequest = new DeleteByQueryRequest("");
		BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
		boolQueryBuilder.must(new TermQueryBuilder("lang", "zh"));
		boolQueryBuilder.must(new TermQueryBuilder("type", "news"));
		deleteByQueryRequest.setQuery(boolQueryBuilder);
		BulkByScrollResponse bulkByScrollResponse = restHighLevelClient.deleteByQuery(deleteByQueryRequest, RequestOptions.DEFAULT);
		System.out.println(bulkByScrollResponse);
	}



	@Test
	void testPa() throws Exception {
		File mdFile = FileUtils.getFile("C:\\CYDev\\workspace\\eulerdoc\\openEuler-portal\\app\\.vitepress\\dist\\zh\\learn\\mooc\\detail\\index.html");
		Map<String, Object> map = EulerParse.parse("zh", "download", mdFile);

		System.out.println(map);


		IndexRequest indexRequest = new IndexRequest(s.index).id(IdUtil.getId()).source(map);

		IndexResponse indexResponse = restHighLevelClient.index(indexRequest, RequestOptions.DEFAULT);
		System.out.println(indexResponse.toString());
//		String s = "012345";
//		System.out.println(s.substring(0, 6));
	}





	@Test
	void ines() throws IOException {
		CreateIndexRequest request1 = new CreateIndexRequest("ddat");
		File mappingJson = FileUtils.getFile("C:\\CYDev\\EaseSearch\\src\\main\\resources\\mapping\\mapping.json");
		String mapping = FileUtils.readFileToString(mappingJson, StandardCharsets.UTF_8);

		request1.mapping(mapping, XContentType.JSON);
		request1.setTimeout(TimeValue.timeValueMillis(1));

		CreateIndexResponse d = restHighLevelClient.indices().create(request1, RequestOptions.DEFAULT);
		System.out.println(d.index());
	}

	@Test
	void testSuggestions() throws IOException {
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		SuggestionBuilder<TermSuggestionBuilder> termSuggestionBuilder =
				SuggestBuilders.termSuggestion("textContent").text("开元 opengausd").minWordLength(2).prefixLength(0).analyzer("ik_smart");

		SuggestBuilder suggestBuilder = new SuggestBuilder();
		suggestBuilder.addSuggestion("my_sugg", termSuggestionBuilder);

		SearchRequest request = new SearchRequest("opengauss_articles_test_zh");

		request.source(searchSourceBuilder.suggest(suggestBuilder));

		SearchResponse response = restHighLevelClient.search(request, RequestOptions.DEFAULT);

		System.out.println(response);
		StringBuilder newKeyword = new StringBuilder();
		for (Suggest.Suggestion.Entry<? extends Suggest.Suggestion.Entry.Option> my_sugg : response.getSuggest().getSuggestion("my_sugg")) {
			String text = my_sugg.getOptions().get(0).getText().string();
		}
	}


	@Test
	void testMigrate() throws IOException {
		//源es
		RestHighLevelClient input = getEsClientSecurity("host", 9200, "username", "password");
		//目标迁移es
		RestHighLevelClient output = getEsClientSecurity("host", 9200, "username", "password");




	}


	public RestHighLevelClient getEsClient(String host, int port) {
		return new RestHighLevelClient(RestClient.builder(new HttpHost(host, port, "http")).setRequestConfigCallback(new RestClientBuilder.RequestConfigCallback() {
			// 该方法接收一个RequestConfig.Builder对象，对该对象进行修改后然后返回。
			@Override
			public RequestConfig.Builder customizeRequestConfig(RequestConfig.Builder requestConfigBuilder) {
				return requestConfigBuilder.setConnectTimeout(5000 * 1000) // 连接超时（默认为1秒）
						.setSocketTimeout(6000 * 1000);// 套接字超时（默认为30秒）//更改客户端的超时限制默认30秒现在改为100*1000分钟
			}
		}));
	}

	public RestHighLevelClient getEsClientSecurity(String host, int port, String username, String password) {
		RestHighLevelClient restClient = null;
		try {
			final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
			credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials("admin", "Cloudfoundry@123"));
			SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {
				@Override
				public boolean isTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
					return true;
				}
			}).build();
			SSLIOSessionStrategy sessionStrategy = new SSLIOSessionStrategy(sslContext, NoopHostnameVerifier.INSTANCE);
			restClient = new RestHighLevelClient(
					RestClient.builder(new HttpHost(host, port, "https")).setHttpClientConfigCallback(
							new RestClientBuilder.HttpClientConfigCallback() {
								@Override
								public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpAsyncClientBuilder) {
									httpAsyncClientBuilder.disableAuthCaching();
									httpAsyncClientBuilder.setSSLStrategy(sessionStrategy);
									httpAsyncClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
									return httpAsyncClientBuilder;
								}
							}
					).setRequestConfigCallback(new RestClientBuilder.RequestConfigCallback() {
						// 该方法接收一个RequestConfig.Builder对象，对该对象进行修改后然后返回。
						@Override
						public RequestConfig.Builder customizeRequestConfig(RequestConfig.Builder requestConfigBuilder) {
							return requestConfigBuilder.setConnectTimeout(5000 * 1000) // 连接超时（默认为1秒）
									.setSocketTimeout(6000 * 1000);// 套接字超时（默认为30秒）//更改客户端的超时限制默认30秒现在改为100*1000分钟
						}
					}));// 调整最大重试超时时间（默认为30秒）.setMaxRetryTimeoutMillis(60000);)


		} catch (Exception e) {
			System.out.println(e.toString());
		}
		return restClient;


	}



}
