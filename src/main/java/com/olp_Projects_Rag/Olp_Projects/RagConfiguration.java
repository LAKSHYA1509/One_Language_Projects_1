package com.olp_Projects_Rag.Olp_Projects;

import java.io.File;
import java.nio.file.Paths;
import java.util.List;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.Resource;


@Configuration
public class RagConfiguration {

    private static final Logger log = LoggerFactory.getLogger(RagConfiguration.class);

    @Value("classpath:/docs/olympic-faq.txt")
    private Resource resource;

    @Value("vectorstore.json")
    private String vectorStoreName;

    @Bean
    SimpleVectorStore simpleVectorStore(EmbeddingModel embeddingModel) {
        SimpleVectorStore simpleVectorStore = new SimpleVectorStore(embeddingModel);
        File vectorStoreFile = getVectorStoreFile();
        if(vectorStoreFile.exists()) {
            log.info("Vector store file exists, loading it");
            simpleVectorStore.load(vectorStoreFile);
        } else {
            log.info("Vector store file does not exist, creating it");
            TextReader textReader = new TextReader(resource);
            textReader.getCustomMetadata().put("filename","olympic-faq.txt");
            List<Document> documents = textReader.read();
            TokenTextSplitter textSplitter = new TokenTextSplitter();
            List<Document> splitedDocuments = textSplitter.split(documents);
            simpleVectorStore.add(splitedDocuments);
            simpleVectorStore.save(vectorStoreFile);
        }
        return simpleVectorStore;
    }

    private File getVectorStoreFile() {
    Path path = Paths.get("src", "main", "resources", "Data");
        String absolutePath = path.toFile().getAbsolutePath() + "/" + vectorStoreName;
        return new File(absolutePath);
    }
}
