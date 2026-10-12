package com.floop.esplayground.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.query_rules.QueryRuleType;
import com.floop.esplayground.enums.Operation;
import com.floop.esplayground.model.Contact;
import com.floop.esplayground.model.SearchFilter;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class DynamicQueryDrill implements CommandLineRunner {

    private final ElasticsearchClient client;

    public DynamicQueryDrill(ElasticsearchClient client) {
        this.client = client;
    }


    @Override
    public void run(String... args) throws Exception {

        List<SearchFilter> filters = List.of(
                new SearchFilter("name", Operation.MATCH, "quliyev"),
                new SearchFilter("createdAt", Operation.GTE, "2024-01-01")
        );

        List<Query> queries = new ArrayList<>();
        for (SearchFilter f : filters) {
            queries.add(toQuery(f));
        }



        SearchResponse<Contact> searchResponse =  client.search(s->s
                .index("playground-contacts")
                .query(q->q.bool(b->b.filter(queries))),
                Contact.class);

        System.out.println("Total: " + searchResponse.hits().total().value());

        for (Hit<Contact> hit : searchResponse.hits().hits()) {
            System.out.println(hit.source().name());
        }


    }

    private Query equalQuery(String field, String value) {
        return Query.of(q->q.term(f->f.field(field).value(value)));

    }

    private Query matchQuery(String field, String value) {

        return Query.of(q->q.match(f->f.field(field).query(value)));
    }

    private Query gteQuery(String field, String value)   {
        return Query.of(q->q.range(d->d.date(f->f.field(field).gte(value))));
    }

    private Query toQuery(SearchFilter f){

        return switch (f.operation()){

            case EQUAL -> equalQuery(f.field(),f.value());
            case MATCH -> matchQuery(f.field(),f.value());
            case GTE -> gteQuery(f.field(), f.value());
        };

    }

}
