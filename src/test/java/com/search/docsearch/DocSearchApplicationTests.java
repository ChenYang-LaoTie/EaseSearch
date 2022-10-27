package com.search.docsearch;

import com.search.docsearch.config.MySystem;
import com.search.docsearch.utils.EulerParse;
import com.search.docsearch.utils.IdUtil;
import org.apache.commons.io.FileUtils;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
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

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
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
}
