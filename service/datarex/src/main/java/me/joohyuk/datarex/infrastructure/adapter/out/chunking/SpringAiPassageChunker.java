package me.joohyuk.datarex.infrastructure.adapter.out.chunking;

import java.util.List;
import me.joohyuk.datarex.domain.port.out.PassageChunker;
import org.springframework.stereotype.Component;

@Component
public class SpringAiPassageChunker implements PassageChunker {

    @Override
    public List<String> chunk(String text, String contentType, String passageVersion) {
        // 예시: passageVersion에 따라 chunkSize/overlap 정책을 바꿀 수 있음
        int chunkSize = 1000;
        int overlap = 150;

        // 아주 단순한 예시 splitter (실전에서는 Spring AI splitter로 대체)
        // Spring AI의 TokenTextSplitter / RecursiveTextSplitter 등을 사용하도록 교체
        return SimpleSplitters.slidingWindow(text, chunkSize, overlap);
    }

    static class SimpleSplitters {

        static List<String> slidingWindow(String text, int size, int overlap) {
            if (text.length() <= size) {
                return List.of(text);
            }
            var out = new java.util.ArrayList<String>();
            int start = 0;
            while (start < text.length()) {
                int end = Math.min(text.length(), start + size);
                out.add(text.substring(start, end));
                if (end == text.length()) {
                    break;
                }
                start = Math.max(0, end - overlap);
            }
            return out;
        }
    }
}
