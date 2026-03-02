package me.joohyuk.datahub.infrastructure.adapter.in.web.dto;

import com.spartaecommerce.domain.vo.UserId;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import me.joohyuk.datahub.application.dto.command.CreateDocumentCollectionCommand;

public record CreateDocumentCollectionRequest(
    @NotBlank(message = "Collection name cannot be blank")
    @Size(max = 255, message = "Collection name must not exceed 255 characters")
    String name,

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    String description
) {

  public CreateDocumentCollectionCommand toCommand(Long userId) {
    return new CreateDocumentCollectionCommand(UserId.of(userId), name, description);
  }
}
