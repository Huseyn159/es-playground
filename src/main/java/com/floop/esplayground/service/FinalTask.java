package com.floop.esplayground.service;


import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.Refresh;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.aggregations.StringTermsBucket;
import co.elastic.clients.elasticsearch.core.*;
import co.elastic.clients.elasticsearch.core.bulk.BulkResponseItem;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.floop.esplayground.model.Contact;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
public class FinalTask implements CommandLineRunner {

    private final ElasticsearchClient client;

    public FinalTask(ElasticsearchClient client) {
        this.client = client;
    }

    @Override
    public void run(String... args) throws IOException {



        List<Contact> contacts = List.of(
                new Contact("Vüsal Quliyev","Ganja","+994701000014",true,"2024-10-01"),
                new Contact("Lalə Məmmədova","Sumgait","+994701000015",false,"2023-09-12")
        );

        BulkRequest.Builder builder = new BulkRequest.Builder();
        int id= 14;
        for(Contact c : contacts) {
            String docId = String.valueOf(id++);

            builder.operations(i -> i.index(idx -> idx
                    .index("playground-contacts")
                    .id(docId)
                    .document(c)));
        }

        BulkResponse response = client.bulk(builder.refresh(Refresh.WaitFor).build());

        if (response.errors()){
            for (BulkResponseItem item : response.items()){
                if (item.error()!=null){
                    System.out.println("Xeta! " + item.id() + " -> sebeb ->" + item.error().reason());
                }

            }
        }
        else {
            System.out.println("Hersey xetasiz yazildi!");
        }


        CountResponse countResponse = client.count(i->i.index("playground-contacts"));
        System.out.println("Umumi say: " + countResponse.count());




        SearchResponse<Contact> axtaris = client.search(i->i.index("playground-contacts")
                        .query(b->b.bool(
                m-> m
                        .mustNot(t->t.term(f->f.field("city").value("Baku")))
                        .filter(r->r.range(d->d.date(f->f.field("createdAt").gte("2024-01-01"))))
                        .filter(t->t.term(f->f.field("active").value(true)))


                ))
                        .sort(t->t.field(f->f.field("createdAt").order(SortOrder.Desc)))
                        .from(0)//  page = 0 size 3, page * size = from
                        .size(3)
                        .aggregations("same_city_contacts",a->a.terms(f->f.field("city")))
                        .aggregations("oldest_created_contact",a->a.min(f->f.field("createdAt"))),

                Contact.class

                );

        SearchResponse<Contact> matchSearch = client
                .search(q->q
                        .index("playground-contacts")
                        .query(m-> m.match(f->f
                                .field("name")
                                .query("quliyev"))),
                        Contact.class);

        System.out.println("Total: " + axtaris.hits().total().value());
        for (Hit<Contact> hit : axtaris.hits().hits()) {
            Contact c = hit.source();
            System.out.println(hit.id() + " | " + c.name() + " | " + c.city() + " | " + c.createdAt());
        }

        for (StringTermsBucket b : axtaris.aggregations()
                .get("same_city_contacts")
                .sterms()
                .buckets().array()) {
            System.out.println(b.key().stringValue() + " → " + b.docCount());
        }

        System.out.println("Ən köhnə: " + axtaris.aggregations()
                .get("oldest_created_contact")
                .min()
                .valueAsString());


        System.out.println("Match neticesi total: " + matchSearch.hits().total().value());
        for (Hit<Contact> hit : matchSearch.hits().hits()){
            System.out.println("Id: " + hit.id() + " name: " + hit.source().name());
        }




        GetResponse<Contact> r = client.get(g->g.index("playground-contacts").id("99"),Contact.class);

        if (!r.found()){
            System.out.println("Tapilmadi");
        }else {
            System.out.println("Axtardiginiz" + r.id()  + " -li kontakt: " + r.source().name() );
        }

    }
}
