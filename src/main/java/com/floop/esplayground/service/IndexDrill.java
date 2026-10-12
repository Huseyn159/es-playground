package com.floop.esplayground.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

public class IndexDrill implements CommandLineRunner {

    private final ElasticsearchClient client;

    public IndexDrill(ElasticsearchClient client) {
        this.client = client;
    }


    @Override
    public void run(String... args) throws Exception {

        boolean exists = client.indices().exists(e -> e.index("java-contacts")).value();

        if (exists) {
            System.out.println("artiq var");
//            client.indices().delete(i->i.index("java-contacts"));
        } else {
            client.indices().create(c -> c
                    .index("java-contacts")
                    .settings(s -> s.numberOfShards("1").numberOfReplicas("0"))
                    .mappings(m -> m
                            .properties("name", p -> p.text(t -> t.fields("raw", f -> f.keyword(k -> k))))
                            .properties("city", p -> p.keyword(k -> k))
                            .properties("number", p -> p.keyword(k -> k))
                            .properties("active", p -> p.boolean_(b -> b))
                            .properties("createdAt", d -> d.date(dt->dt))
                    )
            );

        }
    }
}
