package com.floop.esplayground.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.Refresh;
import co.elastic.clients.elasticsearch.core.*;
import co.elastic.clients.elasticsearch.cluster.HealthResponse;

import co.elastic.clients.elasticsearch.core.bulk.BulkResponseItem;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.floop.esplayground.model.Contact;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Component
public class StartupCheck implements CommandLineRunner {

    private final ElasticsearchClient client;

    public StartupCheck(ElasticsearchClient client) {
        this.client = client;
    }

    @Override
    public void run(String... args) throws IOException {

//        CountResponse countResponse = client.count(c -> c.index("playground-contacts"));
//        HealthResponse health = client.cluster().health();
//        GetResponse<Contact> response = client.get(g -> g.index("playground-contacts").id("3"), Contact.class);
//        System.out.println("Status: " + health.status());
//        System.out.println("Nodes: " + health.numberOfNodes());
//        System.out.println("Found: " + response.found());
//        System.out.println("Source: " + response.source());
//        if (response.found()) {
//            System.out.println("Name: " + response.source().name());
//        } else {
//            System.out.println("Tapılmadı");
//        }
//
//        Contact samir = new Contact("Samir Nəbiyev", "Baku", "+994551112233", true, "2024-09-01");
//
//        IndexResponse r = client.index(i -> i.index("playground-contacts")
//                .id("10")
//                .document(samir));
//
//        System.out.println("Count: " + countResponse.count());
//        System.out.println("Result: " + r.result());
//
//        List<Contact> contacts = List.of(
//                new Contact("Kamran Əliyev", "Baku",  "+994501000011", true,  "dunen"),
//                new Contact("Səbinə Hüseynova", "Ganja", "+994501000012", false, "2023-04-05"),
//                new Contact("Orxan Vəliyev", "Sheki", "+994501000013", true,  "2024-06-20")
//        );
//
//        BulkRequest.Builder br = new BulkRequest.Builder();
//
//        int id = 11;
//        for (Contact c : contacts){
//            String docId = String.valueOf(id++);
//
//            br.operations(op -> op.index(i -> i
//                    .index("playground-contacts")
//                    .id(docId)
//                    .document(c)));
//
//
//
//        }
//
//
//        BulkResponse result = client.bulk(br.refresh(Refresh.WaitFor).build());
//
//        long count = client.count(c -> c.index("playground-contacts")).count();
//        System.out.println("Count: " + count);
//
//        if (result.errors()){
//            for (BulkResponseItem item : result.items()){
//                if (item.error() != null){
//                    System.out.println("Xeta! id = " + item.id() + "  Xeta Sebebi -> " + item.error().reason());
//                }
//            }
//        }else {
//            System.out.println("Hamısı yazıldı ✅");
//        }


        SearchResponse<Contact> resp = client.search(s -> s
                .index("playground-contacts")
                .query(q-> q
                        .match(m -> m.field("name").query("əli"))),
                Contact.class


        );

        SearchResponse<Contact> cityResponse = client.search(s -> s
                .index("playground-contacts")
                .query(q->q.term(t -> t.field("city").value("Baku"))),
                        Contact.class);
        System.out.println("Total matched cities: " + cityResponse.hits().total().value());
        System.out.println("Total matched names: " + resp.hits().total().value());

        for (Hit<Contact> hit : resp.hits().hits()){
            System.out.println(hit.id() + " -> " + hit.source().name());
        }

        for (Hit<Contact> hit : cityResponse.hits().hits()){
            System.out.println(hit.id() + " -> " + hit.source().city());
        }



    }
}
