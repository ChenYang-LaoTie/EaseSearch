package com.search.docsearch;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;

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
import org.elasticsearch.action.search.ClearScrollRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchScrollRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.Scroll;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;

import lombok.extern.slf4j.Slf4j;

@SpringBootTest
@Slf4j
class DocSearchApplicationTests {


//     @Autowired
//     @Qualifier("restHighLevelClient")
//     private RestHighLevelClient restHighLevelClient;

//     public static final String FORUMDOMAIM = "https://forum.openeuler.org";

    
//     @Test
//     public void exportForum()  throws IOException{

//         List<String[]> aal = new ArrayList<>();

//         String[] bt = {"TopicID", "TopicAuthorID", "TopicTitle", "TopicDescription", "TopicTag/Category", "TopicCreateTime", "TopicLink", "PostID", "PostAuthorID", "PostTo", "PostBody", "PostCreateTime", "PostReaction"};
//         aal.add(bt);

//         String path = FORUMDOMAIM + "/latest.json?no_definitions=true&page=";

//         String req = "";

//         HttpURLConnection connection = null;
//         String result;  // 返回结果字符串
//         for (int i = 0; ; i++) {
//             req = path + i;
//             try {
//                 connection = sendHTTP(req, "GET", null, null);
//                 TimeUnit.SECONDS.sleep(30);
//                 if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
//                     result = ReadInput(connection.getInputStream());
//                     if (!setData(result, aal)) {
//                         break;
//                     }
//                 } else {
//                     log.error(req + " - ", connection.getResponseCode());
//                     return;
//                 }
//             } catch (IOException | InterruptedException e) {
//                 log.error("Connection failed, error is: " + e.getMessage());
//                 return;
//             } finally {
//                 if (null != connection) {
//                     connection.disconnect();
//                 }
//             }
//         }
//           //创建excel工作簿
//           XSSFWorkbook workbook = new XSSFWorkbook();
//           //创建工作表sheet
//           XSSFSheet sheet = workbook.createSheet();
//           for (int i = 0; i < aal.size(); i ++) {
//               XSSFRow row = sheet.createRow(i);
//               String[] zz = aal.get(i);
//               for (int j = 0; j < zz.length; j ++) {
//                   XSSFCell cell = row.createCell(j);
//                   cell.setCellValue(zz[j]);
//               }
//           }
//           String filePath = "export.xlsx";
//           File file=new File(filePath);
//           if (!file.exists()) {
//               file.createNewFile();
//           }
//           FileOutputStream fileOut = new FileOutputStream(file);
//           workbook.write(fileOut);
  
//           fileOut.flush();
  
//           fileOut.close();
//           workbook.close();
//     }


//     private boolean setData(String data,  List<String[]> aal) {

//         JSONObject post = JSON.parseObject(data);
//         JSONObject topicList = post.getJSONObject("topic_list");
//         JSONArray jsonArray = topicList.getJSONArray("topics");
//         if (jsonArray.size() <= 0) {
//             return false;
//         }
//         String path = "";
//         HttpURLConnection connection = null;
//         String result;  // 返回结果字符串
//         for (int i = 0; i < jsonArray.size(); i++) {
//             JSONObject topic = jsonArray.getJSONObject(i);
//             String id = topic.getString("id");
//             String slug = topic.getString("slug");
//             path = String.format("%s/t/%s/%s.json?track_visit=true&forceLoad=true", FORUMDOMAIM, slug, id);
//             try {
//                 connection = sendHTTP(path, "GET", null, null);
//                 if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
//                     result = ReadInput(connection.getInputStream());
//                     JSONObject st = JSON.parseObject(result);
//                     JSONObject postStream = st.getJSONObject("post_stream");
//                     JSONArray posts = postStream.getJSONArray("posts");

        
//                     String TopicID = id;
//                     String TopicAuthorID = st.getJSONObject("details").getJSONObject("created_by").getString("username");
//                     String TopicTitle = st.getString("title");

//                     JSONObject td = posts.getJSONObject(0);
//                     String tdc = td.getString("cooked");
//                     Parser aparser = Parser.builder().build();
//                     HtmlRenderer rendererzz = HtmlRenderer.builder().build();
//                     Node documentzzz = aparser.parse(tdc);
//                     Document znode = Jsoup.parse(rendererzz.render(documentzzz));

//                     String TopicDescription = znode.text();

//                     JSONArray tags = st.getJSONArray("tags");

//                     List<String> list = JSON.parseArray(tags.toString(), String.class);
//                     String TopicTag = String.join(",", list);
//                     String created_at = st.getString("created_at");
//                     String TopicCreateTime = ford(created_at);

//                     String TopicLink = String.format("%s/t/%s/%s", FORUMDOMAIM, slug, id);

//                     for (int j = 1; j < posts.size(); j ++) {
//                         JSONObject pt = posts.getJSONObject(j);
//                         String cooked = pt.getString("cooked");
//                         Parser parser = Parser.builder().build();
//                         HtmlRenderer renderer = HtmlRenderer.builder().build();
//                         Node document = parser.parse(cooked);
//                         Document node = Jsoup.parse(renderer.render(document));
//                         if (!StringUtils.hasText(node.text())) {
//                             continue;
//                         }
//                         String PostID = String.valueOf(pt.getInteger("post_number") - 1);
//                         String PostAuthorID = pt.getString("username");
//                         String PostTo = "0";
//                         if (pt.getInteger("reply_to_post_number") != null) {
//                             PostTo = String.valueOf(pt.getInteger("reply_to_post_number") - 1);
//                         }
//                         String PostBody = node.text();

//                         String PostCreateTime = ford(pt.getString("created_at"));

//                         String PostReaction = "0";
//                         JSONArray actions_summary = pt.getJSONArray("actions_summary");
//                         if (actions_summary.size() > 0) {
//                             PostReaction = String.valueOf(actions_summary.getJSONObject(0).getInteger("count"));
//                         }

//                         String[] lie = {TopicID, TopicTitle, TopicAuthorID, TopicDescription, TopicTag, TopicCreateTime, TopicLink, PostID, PostAuthorID, PostTo, PostBody, PostCreateTime, PostReaction};

//                         System.out.println(Arrays.toString(lie));
//                         aal.add(lie);

//                     }
//                 } else {
//                     log.error(path + " - ", connection.getResponseCode());
//                 }
//             } catch (IOException e) {
//                 log.error("Connection failed, error is: " + e.getMessage());
//             } finally {
//                 if (null != connection) {
//                     connection.disconnect();
//                 }
//             }
//         }
//         return true;
//     }

//     private HttpURLConnection sendHTTP(String path, String method, Map<String, String> header, String body) throws IOException {
//         URL url = new URL(path);
//         HttpURLConnection connection = null;
//         connection = (HttpURLConnection) url.openConnection();
//         connection.setRequestMethod(method);
//         if (header != null) {
//             for (Map.Entry<String, String> entry : header.entrySet()) {
//                 connection.setRequestProperty(entry.getKey(), entry.getValue());
//             }
//         }
//         if (StringUtils.hasText(body)) {
//             connection.setDoOutput(true);
//             OutputStream outputStream = connection.getOutputStream();
//             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));
//             writer.write(body);
//             writer.close();
//         }
//         connection.setConnectTimeout(60000);
//         connection.setReadTimeout(60000);
//         connection.connect();
//         return connection;
//     }

//     private String ReadInput(InputStream is) throws IOException {
//         BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
//         StringBuilder sbf = new StringBuilder();
//         String temp = null;
//         while ((temp = br.readLine()) != null) {
//             sbf.append(temp);
//         }
//         try {
//             br.close();
//         } catch (IOException e) {
//             log.error("read input failed, error is: " + e.getMessage());
//         }
//         try {
//             is.close();
//         } catch (IOException e) {
//             log.error("close stream failed, error is: " + e.getMessage());
//         }
//         return sbf.toString();
//     }

//     public String ford(String dateTime) {
//         dateTime = dateTime.replace("Z", " UTC");
//         SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS Z");
//         SimpleDateFormat defaultFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");
//         try {
//             Date time = format.parse(dateTime);
//             String result = defaultFormat.format(time);
//             return result;
//         } catch (Exception e) {
//             e.printStackTrace();
//             return dateTime;
//         }
//     }

//     @Test
//     public void setlog() throws Exception {
//         String index = "index";
        

//         RestHighLevelClient input = getEsClientSecurity("host", 9200, "username", "password");

//         int scrollSize = 500;//一次读取的doc数量
// 		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

//         BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
//         boolQueryBuilder.must(QueryBuilders.termQuery("kubernetes.namespace_name", "website"));
//         boolQueryBuilder.must(QueryBuilders.termQuery("kubernetes.labels.app", "mindsporewebsite"));
// 		searchSourceBuilder.query(boolQueryBuilder);



// 		searchSourceBuilder.size(scrollSize);
// 		Scroll scroll = new Scroll(TimeValue.timeValueMinutes(10));//设置一次读取的最大连接时长

// 		SearchRequest searchRequest = new SearchRequest(index);
// //        searchRequest1.types("_doc");
// 		searchRequest.source(searchSourceBuilder);
// 		searchRequest.scroll(scroll);

// 		SearchResponse searchResponse = input.search(searchRequest, RequestOptions.DEFAULT);

// 		String scrollId = searchResponse.getScrollId();
// 		System.out.println("scrollId - " + scrollId);

// 		SearchHit[] hits = searchResponse.getHits().getHits();
//         File file = new File("C:\\websitelog_20230625.log");
//         //if file doesnt exists, then create it
//         if (!file.exists()) {
//             file.createNewFile();
//         }
//         FileWriter fw = new FileWriter(file, true);

//         while (hits != null && hits.length > 0) {
//             for (SearchHit hit : hits) {
//                 Map<String, Object> sourceAsMap = hit.getSourceAsMap();
//                 String log = (String) sourceAsMap.get("log");

//                 fw.write(log + "\r\n");
//             }
//             SearchScrollRequest scrollRequest = new SearchScrollRequest(scrollId);
//             scrollRequest.scroll(scroll);
//             searchResponse = input.scroll(scrollRequest, RequestOptions.DEFAULT);
//             scrollId = searchResponse.getScrollId();
//             hits = searchResponse.getHits().getHits();
//             System.out.println("hits - " + hits.length);
//         }

//         ClearScrollRequest clearScrollRequest = new ClearScrollRequest();
//         clearScrollRequest.addScrollId(scrollId);
//         input.clearScroll(clearScrollRequest, RequestOptions.DEFAULT);

//     }



    
// 	public RestHighLevelClient getEsClientSecurity(String host, int port, String username, String password) {
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
}
