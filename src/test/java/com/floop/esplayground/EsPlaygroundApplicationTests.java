package com.floop.esplayground;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.Refresh;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import com.floop.esplayground.model.Contact;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PlaygroundTest {

    @Autowired
    private ElasticsearchClient client;

    @Test
    void createAndDeleteIndex() throws IOException {
        String indexName = "playground-" + UUID.randomUUID();
        try {
          client.indices().create(c->c
                  .index(indexName)
                  .mappings(m->m
                          .properties("name",p->p.text(t->t.fields("raw",f->f.keyword(k->k))))
                          .properties("city",p->p.keyword(k->k))
                          .properties("number",p->p.keyword(k->k))
                          .properties("active",p->p.boolean_(b->b))
                          .properties("createdAt",d->d.date(b->b))
                  )

                  );

          assertTrue(client.indices().exists(e->e.index(indexName)).value());

            Contact c1 = new Contact("Ali Veli", "Baku", "+994501111111", true,"2026-01-01");
            Contact c2 = new Contact("Leyla Aliyeva", "Ganja", "+994502222222", false, "2021-09-04");
            Contact c3 = new Contact("Nigar Quliyeva", "Baku", "+994503333333", true, "2001-11-11");
          BulkResponse response = client.bulk(b->b
                    .operations(op->op.index(i->i.index(indexName).id("1").document(c1)))
                    .operations(op->op.index(i->i.index(indexName).id("2").document(c2)))
                    .operations(op->op.index(i->i.index(indexName).id("3").document(c3)))
                    .refresh(Refresh.WaitFor));

          assertFalse(response.errors());
          assertEquals(3,client.count(c->c.index(indexName)).count());

            SearchResponse<Contact> searchResponse = client.search(i->i.index(indexName)
                    .query(t->t
                            .term(f->f
                                    .field("city")
                                    .value("Baku"))),
                    Contact.class);

            assertEquals(2,searchResponse.hits().total().value());
            assertEquals("Baku",searchResponse.hits().hits().get(0).source().city());

        } finally {
            client.indices().delete(d->d.index(indexName));
        }
    }
}
