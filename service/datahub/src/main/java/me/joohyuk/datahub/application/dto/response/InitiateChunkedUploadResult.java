package me.joohyuk.datahub.application.dto.response;

/**
 * 청크 업로드 시작 결과
 *
 * <p>클라이언트가 후속 청크 업로드와 완료 요청에 사용할 정보를 포함합니다.
 *
 * @param uploadId 업로드 세션 식별자. 후속 청크 업로드와 완료 요청에 필수
 * @param chunkSize 서버가 지정한 청크 크기 (바이트). 클라이언트는 이 크기 단위로 파일을 분할해야 함
 * @param totalChunks 파일 전체를 업로드하는 데 필요한 총 청크 수
 * @param totalSize 파일 전체 크기 (바이트)
 */
public record InitiateChunkedUploadResult(
    String uploadId,
    int chunkSize,
    int totalChunks,
    long totalSize
) {}