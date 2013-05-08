import org.apache.solr.common.*;
import org.apache.solr.core.CoreContainer;
import org.apache.solr.client.solrj.*;
import org.apache.solr.client.solrj.beans.Field;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import java.io.FileNotFoundException;

import java.io.File;

public class ClientSolrj {

    public static void main(String[] args) {
        if (args.length == 0){
            runRemoteExample(); //connect to localhost
        } else{
            runEmbeddedExample(args[0]);
        }
    }

    private static void runRemoteExample() {
        SolrServer solr = new HttpSolrServer("http://localhost:8983/solr/solrj");
        listCollectionContent(solr, "Running in remote mode");

        SolrInputDocument doc = new SolrInputDocument();
        doc.addField("id", "email3");
        doc.addField("addr_from", "maija@acme.example.com");
        doc.addField("addr_to","kari@acme.example.com");
        doc.addField("addr_to","vania@home.example.com");
        doc.addField("subject","Re: Updating vacancy description");
        try {
            solr.add(doc);
            solr.commit();
        } catch (Exception e) {
            //Catch and ignore over-generic exception. Do NOT do this in production
            e.printStackTrace();
        }
        listCollectionContent(solr, "Added record in remote mode");
        solr.shutdown();
    }

    public static class Email{
        @Field String id, addr_from, subject;
        @Field String[] addr_to;
    }
    private static void runEmbeddedExample(String solrHomePath) {
        File coreConfig = new File(solrHomePath, "solr-solrj.xml" );
        CoreContainer container = null;
        try {
            container = new CoreContainer( solrHomePath, coreConfig);
        } catch (FileNotFoundException ex) {throw new RuntimeException(ex);}
        EmbeddedSolrServer solr = new EmbeddedSolrServer( container, "solrj" );
        listCollectionContent(solr, "Running in embedded mode");
        Email doc = new Email();
        doc.id = "email4";
        doc.addr_from = "kari@acme.example.com";
        doc.addr_to = new String[]{"maija@acme.example.com", "vania@home.example.com"};
        doc.subject = "Thanks (Was Re: Updating vacancy description)";
        try {
            solr.addBean(doc);
            solr.commit();
        } catch (Exception e) {
            //Catch and ignore over-generic exception. Do NOT do this in production
            e.printStackTrace();
        }
        listCollectionContent(solr, "Added record in embedded mode");

        solr.shutdown();
    }

    private static void listCollectionContent(SolrServer solr, String message) {
        System.out.println("\n------------------\n");
        System.out.println(message);
        SolrQuery parameters = new SolrQuery();
        parameters.set("q", "*:*");
        QueryResponse response = null;
        try {
            response = solr.query(parameters);
        } catch (SolrServerException e) {
            e.printStackTrace();
        }
        SolrDocumentList list = response.getResults();
        for (SolrDocument doc: list)
        {
            System.out.println(doc);
        }
        System.out.println("\n------------------\n");
    }
}
