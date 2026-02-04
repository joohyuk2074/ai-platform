package me.joohyuk.datarex.infrastructure.adapter.out.chunking;

import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import me.joohyuk.datarex.domain.model.DocumentContent;
import me.joohyuk.datarex.domain.port.out.chunking.DocumentTransformer;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.stereotype.Component;

/**
 * Spring AI의 TokenTextSplitter를 사용하는 DocumentTransformer 구현체
 * 도메인 모델 DocumentContent와 Spring AI Document 간 변환을 처리
 */
@Slf4j
@Component
public class SpringAiDocumentTransformer implements DocumentTransformer {

    @Override
    public List<DocumentContent> transform(List<DocumentContent> documents, ChunkingConfig config) {
        log.debug(
            "문서 변환(청킹) 시작 - 문서 수: {}, 청크 크기: {}, 최소 문자 수: {}",
            documents.size(),
            config.defaultChunkSize(),
            config.minChunkSizeChars()
        );

        // 1. DocumentContent -> Spring AI Document 변환
        List<Document> springAiDocuments = toSpringAiDocuments(documents);

        // 2. Spring AI TokenTextSplitter로 청킹
        TokenTextSplitter tokenTextSplitter = new TokenTextSplitter(
            config.defaultChunkSize(),
            config.minChunkSizeChars(),
            config.minChunkLengthToEmbed(),
            config.maxNumChunks(),
            config.keepSeparator()
        );

        List<Document> splitDocuments = tokenTextSplitter.apply(springAiDocuments);

        // 3. Spring AI Document -> DocumentContent 변환
        List<DocumentContent> result = toDocumentContents(splitDocuments);

        log.debug("문서 청킹 완료 - 생성된 청크 수: {}", result.size());

        return result;
    }

    /**
     * 도메인 DocumentContent를 Spring AI Document로 변환
     */
    private List<Document> toSpringAiDocuments(List<DocumentContent> documents) {
        return documents.stream()
            .map(doc -> new Document(doc.content(), doc.metadata()))
            .collect(Collectors.toList());
    }

    /**
     * Spring AI Document를 도메인 DocumentContent로 변환
     */
    private List<DocumentContent> toDocumentContents(List<Document> documents) {
        return documents.stream()
            .map(doc -> new DocumentContent(doc.getText(), doc.getMetadata()))
            .collect(Collectors.toList());
    }
}