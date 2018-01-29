package solr;

import org.apache.commons.io.FileUtils;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.common.SolrDocumentList;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SolrConnector {
    public static List<String> callSolr() {
        SolrClient client = new HttpSolrClient("http://hs1.yatrus.com:8983/solr/Twitter3");
        String queryStr = "SentimentDocumentString:POSITIVE";
        SolrQuery query = new SolrQuery(queryStr);
        query.setFilterQueries("Language:en");
        query.setSort("timestamp", SolrQuery.ORDER.desc);
        query.setRows(300);
        query.setFields("Title");
        SolrDocumentList result;
        List<String> positiveTitles = new ArrayList<>();
        List<String> newList = new ArrayList<>();

        try {
            result = client.query(query).getResults();
            for (int i = 0; i < result.size(); i++) {
                positiveTitles.add(result.get(i).entrySet().iterator().next().getValue().toString());
                String newTitle = null;
                for(String title : positiveTitles){
                    newTitle = String.format("%s %s", "2", title);
                }
                newList.add(newTitle);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        try {
            FileUtils.writeLines(new File("/home/zealot/IdeaProjects/spark_twitter_streaming/src/main/resources/training/positive_tweets.txt"),
                    newList);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return newList;
    }
}
