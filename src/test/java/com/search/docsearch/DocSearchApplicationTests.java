package com.search.docsearch;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.search.docsearch.config.MySystem;
import com.search.docsearch.parse.OPENEULER;
import com.search.docsearch.service.DataImportService;
import lombok.extern.slf4j.Slf4j;
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
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
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
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.StringUtils;

import javax.net.ssl.SSLContext;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

@SpringBootTest
@Slf4j
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
			SearchResponse searchScrollResponseS = input.scroll(searchScrollRequestS, RequestOptions.DEFAULT);
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
			input.clearScroll(clearScrollRequest, RequestOptions.DEFAULT);
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
	public void myTest() throws IOException {
		File file = FileUtils.getFile("C:\\CYDev\\workspace\\website-docs\\public\\docs\\zh-CN\\r2.0\\api_python\\mindspore.html");
		String fileContent = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
//		System.out.println(fileContent);

		Document node = Jsoup.parse(fileContent);

		Elements sections = node.getElementsByClass("section");

		if (sections.size() > 0) {
			Element one = sections.get(0);
			System.out.println(one.toString());
		}
	}

	public static final String MINDSPORE_OFFICIAL = "https://www.mindspore.cn";


	public static final String FORUMDOMAIM = "https://forum.openeuler.org";

	@Test
	public void exportForum() throws IOException {
		List<String[]> aal = new ArrayList<>();

		String[] bt = {"TopicID", "TopicAuthorID", "TopicDescription", "TopicTag/Category", "TopicCreateTime", "TopicLink", "PostID", "PostAuthorID", "PostTo", "PostBody", "PostCreateTime", "PostReaction"};
		aal.add(bt);

		String path = FORUMDOMAIM + "/latest.json?no_definitions=true&page=";

		String req = "";
		HttpURLConnection connection = null;
		String result;  // 返回结果字符串

		for (int i = 0; ; i++) {
			req = path + i;
			try {
				connection = sendHTTP(req, "GET", null, null);
				TimeUnit.SECONDS.sleep(30);
				if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
					result = ReadInput(connection.getInputStream());
					if (!setData(result, aal)) {
						break;
					}
				} else {
					log.error(req + " - ", connection.getResponseCode());
					return;
				}
			} catch (IOException | InterruptedException e) {
				log.error("Connection failed, error is: " + e.getMessage());
				return;
			} finally {
				if (null != connection) {
					connection.disconnect();
				}
			}
		}
		//创建excel工作簿
		XSSFWorkbook workbook = new XSSFWorkbook();
		//创建工作表sheet
		XSSFSheet sheet = workbook.createSheet();
		for (int i = 0; i < aal.size(); i++) {
			XSSFRow row = sheet.createRow(i);
			String[] zz = aal.get(i);
			for (int j = 0; j < zz.length; j++) {
				XSSFCell cell = row.createCell(j);
				cell.setCellValue(zz[j]);
			}
		}
		String filePath = "export.xlsx";
		File file = new File(filePath);
		if (!file.exists()) {
			file.createNewFile();
		}
		FileOutputStream fileOut = new FileOutputStream(file);
		workbook.write(fileOut);

		fileOut.flush();

		fileOut.close();
		workbook.close();
	}

	private boolean setData(String data, List<String[]> aal) {

		JSONObject post = JSON.parseObject(data);
		JSONObject topicList = post.getJSONObject("topic_list");
		JSONArray jsonArray = topicList.getJSONArray("topics");
		if (jsonArray.size() <= 0) {
			return false;
		}

		String path = "";
		HttpURLConnection connection = null;
		String result;  // 返回结果字符串

		for (int i = 0; i < jsonArray.size(); i++) {
			JSONObject topic = jsonArray.getJSONObject(i);
			String id = topic.getString("id");
			String slug = topic.getString("slug");
			path = String.format("%s/t/%s/%s.json?track_visit=true&forceLoad=true", FORUMDOMAIM, slug, id);
			try {
				connection = sendHTTP(path, "GET", null, null);
				if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
					result = ReadInput(connection.getInputStream());
					JSONObject st = JSON.parseObject(result);
					JSONObject postStream = st.getJSONObject("post_stream");
					JSONArray posts = postStream.getJSONArray("posts");

					String TopicID = id;
					String TopicAuthorID = st.getJSONObject("details").getJSONObject("created_by").getString("username");
					String TopicTitle = st.getString("title");

					JSONObject td = posts.getJSONObject(0);
					String tdc = td.getString("cooked");
					Parser aparser = Parser.builder().build();
					HtmlRenderer rendererzz = HtmlRenderer.builder().build();
					Node documentzzz = aparser.parse(tdc);
					Document znode = Jsoup.parse(rendererzz.render(documentzzz));

					String TopicDescription = znode.text();

					JSONArray tags = st.getJSONArray("tags");

					List<String> list = JSON.parseArray(tags.toString(), String.class);
					String TopicTag = String.join(",", list);
					String created_at = st.getString("created_at");
					String TopicCreateTime = ford(created_at);

					String TopicLink = String.format("%s/t/%s/%s", FORUMDOMAIM, slug, id);

					for (int j = 1; j < posts.size(); j++) {
						JSONObject pt = posts.getJSONObject(j);
						String cooked = pt.getString("cooked");
						Parser parser = Parser.builder().build();
						HtmlRenderer renderer = HtmlRenderer.builder().build();
						Node document = parser.parse(cooked);
						Document node = Jsoup.parse(renderer.render(document));

						if (!StringUtils.hasText(node.text())) {
							continue;
						}
						String PostID = String.valueOf(pt.getInteger("post_number") - 1);
						String PostAuthorID = pt.getString("username");
						String PostTo = "0";
						if (pt.getInteger("reply_to_post_number") != null) {
							PostTo = String.valueOf(pt.getInteger("reply_to_post_number") - 1);
						}

						String PostBody = node.text();

						String PostCreateTime = ford(pt.getString("created_at"));

						String PostReaction = "0";
						JSONArray actions_summary = pt.getJSONArray("actions_summary");
						if (actions_summary.size() > 0) {
							PostReaction = String.valueOf(actions_summary.getJSONObject(0).getInteger("count"));
						}

						String[] lie = {TopicID, TopicAuthorID, TopicDescription, TopicTag, TopicCreateTime, TopicLink, PostID, PostAuthorID, PostTo, PostBody, PostCreateTime, PostReaction};

						System.out.println(Arrays.toString(lie));
						aal.add(lie);

					}
				} else {
					log.error(path + " - ", connection.getResponseCode());
				}
			} catch (IOException e) {
				log.error("Connection failed, error is: " + e.getMessage());
			} finally {
				if (null != connection) {
					connection.disconnect();
				}
			}
		}

		return true;
	}


	private HttpURLConnection sendHTTP(String path, String method, Map<String, String> header, String body) throws IOException {
		URL url = new URL(path);
		HttpURLConnection connection = null;
		connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod(method);

		if (header != null) {
			for (Map.Entry<String, String> entry : header.entrySet()) {
				connection.setRequestProperty(entry.getKey(), entry.getValue());
			}
		}

		if (StringUtils.hasText(body)) {
			connection.setDoOutput(true);
			OutputStream outputStream = connection.getOutputStream();
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));
			writer.write(body);
			writer.close();
		}

		connection.setConnectTimeout(60000);
		connection.setReadTimeout(60000);
		connection.connect();
		return connection;
	}

	private String ReadInput(InputStream is) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
		StringBuilder sbf = new StringBuilder();
		String temp = null;
		while ((temp = br.readLine()) != null) {
			sbf.append(temp);
		}
		try {
			br.close();
		} catch (IOException e) {
			log.error("read input failed, error is: " + e.getMessage());
		}
		try {
			is.close();
		} catch (IOException e) {
			log.error("close stream failed, error is: " + e.getMessage());
		}
		return sbf.toString();

	}


	public String ford(String dateTime) {
		dateTime = dateTime.replace("Z", " UTC");
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS Z");
		SimpleDateFormat defaultFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");
		try {
			Date time = format.parse(dateTime);
			String result = defaultFormat.format(time);
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			return dateTime;
		}
	}

	@Test
	public void DDd() throws IOException {
		File file = new File("export_en.jsonl");

		if (!file.exists()) {    //文件不存在则创建文件，先创建目录
			file.createNewFile();
		}

		BufferedWriter bw = new BufferedWriter(new FileWriter(file.getPath()));
		System.out.println("begin --------");
		long st = System.currentTimeMillis();

		RestHighLevelClient input = getEsClientSecurity("", 9200, "", "");

		String index = "opengauss_articles_en";

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
			Map<String, Object> mm = hit.getSourceAsMap();
			JSONObject json = new JSONObject();
			json.put("prompt", mm.get("title"));
			json.put("completion", mm.get("textContent"));
			String type = (String)mm.get("type");
			switch (type) {
				case "docs" -> json.put("path", "https://docs.opengauss.org/" + mm.get("path"));
				default -> json.put("path", "https://opengauss.org/" + mm.get("path"));
			}


			String zzz = json.toJSONString();
			System.out.println(zzz);

			bw.write(zzz + "\n");

		}

		while (hits.length > 0) {
			SearchScrollRequest searchScrollRequestS = new SearchScrollRequest(scrollId);
			searchScrollRequestS.scroll(scroll);
			SearchResponse searchScrollResponseS = input.scroll(searchScrollRequestS, RequestOptions.DEFAULT);
			scrollId = searchScrollResponseS.getScrollId();
			System.out.println("scrollId - " + scrollId);

			hits = searchScrollResponseS.getHits().getHits();
			System.out.println("hits - " + hits.length);

			BulkRequest bulkRequestS = new BulkRequest();
			for (SearchHit hit : hits) {
				Map<String, Object> mm = hit.getSourceAsMap();
				JSONObject json = new JSONObject();
				json.put("prompt", mm.get("title"));
				json.put("completion", mm.get("textContent"));
				String type = (String)mm.get("type");
				switch (type) {
					case "docs" -> json.put("path", "https://docs.opengauss.org/" + mm.get("path"));
					default -> json.put("path", "https://opengauss.org/" + mm.get("path"));
				}


				String zzz = json.toJSONString();
				System.out.println(zzz);

				bw.write(zzz + "\n");

			}

		}

		ClearScrollRequest clearScrollRequest = new ClearScrollRequest();
		clearScrollRequest.addScrollId(scrollId);
		try {
			input.clearScroll(clearScrollRequest, RequestOptions.DEFAULT);
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("读取用时:" + (System.currentTimeMillis() - st));

		bw.close();
	}

	@Test
	public void aa() {
		System.out.println("zzzzzzzzzzzzzzzzzz");
		try {
			long startTime = System.currentTimeMillis();
			String url = "http://127.0.0.1:8089/search/docs";
			String jsonPayload = "{\"keyword\":\"生命周期\",\"page\":1,\"pageSize\":10,\"lang\":\"zh\",\"type\":\"\"}";

			HttpClient httpClient = HttpClient.newBuilder().build();

			// 构建多个请求
			for (int i = 0; i < 10000; i++) {
				HttpRequest request = HttpRequest.newBuilder()
						.uri(URI.create(url))
						.header("Content-Type", "application/json")
						.POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
						.build();
				HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

				int statusCode = response.statusCode();
				HttpHeaders headers = response.headers();
				String responseBody = response.body();

				System.out.println("Status Code: " + statusCode);
				System.out.println("Headers: " + headers);
				System.out.println("Response Body: " + responseBody);
				System.out.println();
			}
			long endTime = System.currentTimeMillis();
			long durationMillis = endTime - startTime;
			double durationSeconds = durationMillis / 1000.0;

			System.out.println("代码块执行时间：" + durationSeconds + " 秒");
		} catch (Exception e) {
			e.printStackTrace();
		}

	}


}