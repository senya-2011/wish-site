package com.dabwish.dabwish.repository

import com.dabwish.dabwish.model.wish.WishDoc
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.elasticsearch.annotations.Query
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository

interface WishElasticsearchRepository : ElasticsearchRepository<WishDoc, Long> {

    @Query("""
        {
          "bool": {
            "should": [
              {
                "multi_match": {
                  "query": "?0",
                  "fields": ["title^4", "description^3"],
                  "type": "best_fields",
                  "operator": "or"
                }
              },
              {
                "match_phrase": {
                  "title": {
                    "query": "?0",
                    "boost": 3
                  }
                }
              },
              {
                "match_phrase": {
                  "description": {
                    "query": "?0",
                    "boost": 2
                  }
                }
              },
              {
                "match_phrase_prefix": {
                  "title": {
                    "query": "?0",
                    "boost": 2.5,
                    "max_expansions": 50
                  }
                }
              }
            ],
            "minimum_should_match": 1
          }
        }
    """)
    fun searchByQuery(query: String, pageable: Pageable): Page<WishDoc>
}

