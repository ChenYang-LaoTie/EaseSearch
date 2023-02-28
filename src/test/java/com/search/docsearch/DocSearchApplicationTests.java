package com.search.docsearch;

import com.search.docsearch.config.MySystem;
import com.search.docsearch.service.DataImportService;
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
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.ClearScrollRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchScrollRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.elasticsearch.search.Scroll;
import org.elasticsearch.search.SearchHit;
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
import java.util.HashSet;

@SpringBootTest
class DocSearchApplicationTests {
	@Autowired
	@Qualifier("restHighLevelClient")
	private RestHighLevelClient restHighLevelClient;

	@Autowired
	@Qualifier("setConfig")
	private MySystem s;

	@Autowired
	public DataImportService dataImportService;



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
	void ines() throws IOException {
		CreateIndexRequest request1 = new CreateIndexRequest("ddat");
		File mappingJson = FileUtils.getFile("");
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
				SuggestBuilders.termSuggestion("textContent").text("").minWordLength(2).prefixLength(0).analyzer("ik_smart");

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
		System.out.println("begin --------");
		long st = System.currentTimeMillis();
		//源es
		RestHighLevelClient input = getEsClientSecurity("host", 9200, "username", "password");
		//目标迁移es
		RestHighLevelClient output = getEsClientSecurity("host", 9200, "username", "password");
//		RestHighLevelClient output = getEsClient("127.0.0.1", 9200);
		//你需要迁移的index
		String index = "mindspore_articles";

		int scrollSize = 500;//一次读取的doc数量
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		searchSourceBuilder.query(QueryBuilders.matchAllQuery());//读取全量数据
		searchSourceBuilder.size(scrollSize);
		Scroll scroll = new Scroll(TimeValue.timeValueMinutes(10));//设置一次读取的最大连接时长

		SearchRequest searchRequest = new SearchRequest(index);
//        searchRequest1.types("_doc");
		searchRequest.source(searchSourceBuilder);
		searchRequest.scroll(scroll);

		SearchResponse searchResponse = input.search(searchRequest, RequestOptions.DEFAULT);

		String scrollId = searchResponse.getScrollId();
		System.out.println("scrollId - " + scrollId);

		SearchHit[] hits = searchResponse.getHits().getHits();
		System.out.println("hits - " + hits.length);
		BulkRequest bulkRequest = new BulkRequest();
		for (SearchHit hit : hits) {
			IndexRequest indexRequest = new IndexRequest(index).source(hit.getSourceAsMap());

			bulkRequest.add(indexRequest);
		}
		if (bulkRequest.requests().size() > 0) {
			BulkResponse q = output.bulk(bulkRequest, RequestOptions.DEFAULT);

			System.out.println("wrong ? " + q.hasFailures());
		}

		while (hits.length > 0) {
			SearchScrollRequest searchScrollRequestS = new SearchScrollRequest(scrollId);
			searchScrollRequestS.scroll(scroll);
			SearchResponse searchScrollResponseS = input.scroll(searchScrollRequestS,RequestOptions.DEFAULT);
			scrollId = searchScrollResponseS.getScrollId();
			System.out.println("scrollId - " + scrollId);

			hits = searchScrollResponseS.getHits().getHits();
			System.out.println("hits - " + hits.length);

			BulkRequest bulkRequestS = new BulkRequest();
			for (SearchHit hit : hits) {
				IndexRequest indexRequest = new IndexRequest(index).source(hit.getSourceAsMap());

				bulkRequestS.add(indexRequest);
			}
			if (bulkRequestS.requests().size() > 0) {
				BulkResponse q = output.bulk(bulkRequestS, RequestOptions.DEFAULT);

				System.out.println("wrong ? " + q.hasFailures());
			}

		}

		ClearScrollRequest clearScrollRequest = new ClearScrollRequest();
		clearScrollRequest.addScrollId(scrollId);
		try {
			input.clearScroll(clearScrollRequest,RequestOptions.DEFAULT);
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("读取用时:" + (System.currentTimeMillis() - st));

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
			credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));
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
							return requestConfigBuilder.setConnectTimeout(5000 * 1000) // 连接超时（默认为1秒）因为有些es游标读取非常慢，现改为5000秒
									.setSocketTimeout(6000 * 1000);// 套接字超时（默认为30秒）因为有些es游标读取非常慢，更改客户端的超时限制默认30秒现在改为6000秒
						}
					}));
			// 调整最大重试超时时间（默认为30秒）.setMaxRetryTimeoutMillis(60000);)这条可看情况添加


		} catch (Exception e) {
			System.out.println(e.toString());
		}
		return restClient;


	}


	@Test
	void testNy() throws IOException {

//		DeleteByQueryRequest deleteByQueryRequest = new DeleteByQueryRequest(s.getIndex() + "_*");
//		deleteByQueryRequest.setQuery(QueryBuilders.matchAllQuery());
//		BulkByScrollResponse bulkByScrollResponse = restHighLevelClient.deleteByQuery(deleteByQueryRequest, RequestOptions.DEFAULT);
//		System.out.println(bulkByScrollResponse.getDeleted());
//		OPENEULER openeuler = new OPENEULER();
//		openeuler.customizeData();

		dataImportService.deleteExpired(new HashSet<>());
	}


}
