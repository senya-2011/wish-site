package com.dabwish.dabwish.model.wish

import org.springframework.data.annotation.Id
import org.springframework.data.elasticsearch.annotations.Document
import org.springframework.data.elasticsearch.annotations.Field
import org.springframework.data.elasticsearch.annotations.FieldType
import org.springframework.data.elasticsearch.annotations.Setting
import java.math.BigDecimal
import java.time.OffsetDateTime

@Document(indexName = "wishes")
@Setting(settingPath = "/elasticsearch/wishes-settings.json")
data class WishDoc(
    @Id
    val id: Long,

    @Field(type = FieldType.Long)
    val ownerId: Long,

    @Field(
        type = FieldType.Text,
        analyzer = "ngram_analyzer",
        searchAnalyzer = "standard"
    )
    val title: String,

    @Field(
        type = FieldType.Text,
        analyzer = "ngram_analyzer",
        searchAnalyzer = "standard"
    )
    val description: String?,

    @Field(type = FieldType.Keyword)
    val photoUrl: String?,

    @Field(type = FieldType.Double)
    val price: BigDecimal?,

    @Field(type = FieldType.Date)
    val createdAt: OffsetDateTime,

    @Field(type = FieldType.Date)
    val updatedAt: OffsetDateTime?,
)

