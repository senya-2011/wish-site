package com.dabwish.dabwish.config

import com.dabwish.dabwish.repository.WishElasticsearchRepository
import com.dabwish.dabwish.service.WishService
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(
    value = ["app.elasticsearch.auto-reindex"],
    havingValue = "true",
    matchIfMissing = true
)
class ElasticsearchInitializer(
    private val wishService: WishService,
    @Lazy private val wishElasticsearchRepository: WishElasticsearchRepository?,
) : ApplicationRunner {

    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun run(args: ApplicationArguments?) {
        if (wishElasticsearchRepository == null) {
            logger.info("Elasticsearch repository not available, skipping reindex")
            return
        }

        try {
            logger.info("Starting Elasticsearch reindex...")
            wishService.reindexAllWishes()
            logger.info("Elasticsearch reindex completed successfully")
        } catch (e: Exception) {
            logger.warn("Failed to reindex Elasticsearch on startup: ${e.message}", e)
        }
    }
}

