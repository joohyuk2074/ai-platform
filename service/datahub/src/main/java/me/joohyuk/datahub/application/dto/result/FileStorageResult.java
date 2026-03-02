package me.joohyuk.datahub.application.dto.result;

import com.spartaecommerce.domain.vo.ContentHash;

public record FileStorageResult(
    String fileKey,
    ContentHash contentHash
) {

}
