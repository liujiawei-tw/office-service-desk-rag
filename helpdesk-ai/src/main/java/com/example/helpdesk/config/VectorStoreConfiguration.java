package com.example.helpdesk.config;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** 建立 MVP 使用的記憶體向量資料庫。 */
@Configuration
public class VectorStoreConfiguration {

    /** 使用本機 BAAI 模型建立可進行相似度搜尋的向量庫。 */
    @Bean
    SimpleVectorStore knowledgeVectorStore(EmbeddingModel embeddingModel) {
        return SimpleVectorStore.builder(embeddingModel).build();
    }
}

