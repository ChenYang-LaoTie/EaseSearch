package com.search.docsearch;

import com.search.docsearch.constant.EulerTypeConstants;
import com.search.docsearch.utils.IdUtil;
import org.apache.commons.io.FileUtils;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SpringBootTest
class DocSearchApplicationTests {
	@Autowired
	@Qualifier("restHighLevelClient")
	private RestHighLevelClient restHighLevelClient;

	@Test
	void contextLoads() throws IOException {

		DeleteByQueryRequest deleteByQueryRequest = new DeleteByQueryRequest(EulerTypeConstants.INDEX);
		BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
		boolQueryBuilder.must(new TermQueryBuilder("lang", "zh"));
		boolQueryBuilder.must(new TermQueryBuilder("type", "news"));
		deleteByQueryRequest.setQuery(boolQueryBuilder);
		BulkByScrollResponse bulkByScrollResponse = restHighLevelClient.deleteByQuery(deleteByQueryRequest, RequestOptions.DEFAULT);
		System.out.println(bulkByScrollResponse);
	}



//	@Test
//	void testPa() throws IOException {
//		File mdFile = FileUtils.getFile("");
//		String fileContent = FileUtils.readFileToString(mdFile, StandardCharsets.UTF_8);
//
//		Parser parser = Parser.builder().build();
//		HtmlRenderer renderer = HtmlRenderer.builder().build();
//
//		Node document = parser.parse(fileContent);
//
//		Document node = Jsoup.parse(renderer.render(document));
//		System.out.println(node);
//		Element tags = node.getElementsByTag("hr").first().nextElementSibling();
//		String r = "";
//		System.out.println(tags);
//
//	}





	@Test
	void ines() throws IOException {

		BulkRequest bulkRequest = new BulkRequest();


		for (int i=1; i < 10; i ++) {
			Map<String, Object> jsonMap = new HashMap<>();
			jsonMap.put("lang", "zh");
			jsonMap.put("type", "tt");
			jsonMap.put("in", i);
			IndexRequest indexRequest = new IndexRequest("cytest").source(jsonMap);
			bulkRequest.add(indexRequest);
		}



		if (bulkRequest.requests().size() > 0) {
			restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
		}
	}


	public static String EuleGetValue(String r, String t) {
		if (!r.contains(t)) {
			return "";
		}
		String m = ":";

		r = r.substring(r.indexOf(t) + t.length());
		r = r.substring(r.indexOf(m) + m.length());

		if (!r.contains(":")) {
			return r.trim().replaceAll("\"", "");
		}

		r = r.substring(0, r.indexOf(":"));
		r = r.substring(0, r.lastIndexOf(" ")).trim();
		System.out.println(r);
		r = r.replaceAll("\"", "");
		System.out.println(r);
		return r;
	}
}
