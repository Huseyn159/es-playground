package com.floop.esplayground.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.Refresh;
import co.elastic.clients.elasticsearch.core.BulkRequest;

import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.bulk.BulkResponseItem;
import com.floop.esplayground.model.Contact;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

public class BulkDrill implements CommandLineRunner {

    private final ElasticsearchClient client;

    public BulkDrill(ElasticsearchClient client) {
        this.client = client;
    }

    @Override
    public void run(String... args) throws IOException {

        //Data
        List<Contact> contacts = List.of(
                new Contact("Drill", "Baku", "+994511000001", true, "2012-03-04"),
                new Contact("Lalə Məmmədova", "Sumgait", "+9947010320015", false, "1az")
        );

        BulkRequest.Builder builder = new BulkRequest.Builder();

        int id = 20;

        for(Contact c : contacts){
            String docId = String.valueOf(id++);

            builder.operations(i -> i.delete(idx -> idx
                    .index("playground-contacts")
                    .id(docId)
                    ));
        }

        BulkResponse response = client.bulk(builder.refresh(Refresh.WaitFor).build());

        if (response.errors()){
        for (BulkResponseItem item : response.items()){

                if (item.error()!= null){
                    System.out.println("Xeta! " + item.id() + " -> id-li itemda. Sebebi -> " +item.error());
                }
            }
        }
        else {
            System.out.println("Xeta yoxdur,butun itemlar xetasiz silindi!");
        }

        for (BulkResponseItem item : response.items()){

                if (item.result() != null) {
                    System.out.println("id: " + item.id() + " bu item " + item.result() + " olundu");
                }

        }





    }
}