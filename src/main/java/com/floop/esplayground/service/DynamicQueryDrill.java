package com.floop.esplayground.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.floop.esplayground.enums.Operation;
import com.floop.esplayground.model.Contact;
import com.floop.esplayground.model.SearchFilter;
import com.floop.esplayground.model.SearchResult;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.IOException;
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
                new SearchFilter("city", Operation.IN, null, List.of("Ganja", "Sheki")),
                new SearchFilter("active", Operation.EQUAL, "true", null),
                new SearchFilter("createdAt", Operation.GTE, "2024-01-01", null)
        );

        SearchResult result = search(filters, 0, 2);
        System.out.println("Total: " + result.total());
        result.items().forEach(c -> System.out.println(c.name() + " | " + c.createdAt()));


    }

    private Query inQuery(String field,List<String> values){

        List<FieldValue> fieldValues = values.stream().map(FieldValue ::of).toList();

        return Query.of(t->t
                .terms(f->f
                        .field(field)
                        .terms(v->v
                                .value(fieldValues))));


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
    private Query lteQuery(String field,String value){
        return Query.of(q->q.range(d->d.date(f->f.field(field).lte(value))));
    }

    private Query toQuery(SearchFilter f){

        return switch (f.operation()){

            case EQUAL -> equalQuery(f.field(),f.value());
            case MATCH -> matchQuery(f.field(),f.value());
            case GTE -> gteQuery(f.field(), f.value());
            case IN -> inQuery(f.field(),f.values());
            case LTE -> lteQuery(f.field(), f.value());
        };

    }

    private SearchResult search(List<SearchFilter> filters, int page, int size) throws IOException {
       List<Query> queries = new ArrayList<>();

       for (SearchFilter f : filters){
           queries.add(toQuery(f));
       }

      SearchResponse<Contact> contact = client.search(i->i
              .index("playground-contacts")
              .query(q->q.bool(f->f.filter(queries)))
              .sort(so->so.field(f->f.field("createdAt").order(SortOrder.Desc)))
              .from(page*size)
              .size(size),
              Contact.class);


       List<Contact> items = new ArrayList<>();

       for (Hit<Contact> c : contact.hits().hits()){
           items.add(c.source());
       }

       long total = contact.hits().total().value();

       return new SearchResult(total,items);

    }

}
