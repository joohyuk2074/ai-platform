package me.joohyuk.datahub.application.port.in.service;

import java.io.InputStream;
import com.spartaecommerce.domain.vo.DocumentId;

public interface DocumentQueryService {

  InputStream downloadDocument(DocumentId documentId);
}
