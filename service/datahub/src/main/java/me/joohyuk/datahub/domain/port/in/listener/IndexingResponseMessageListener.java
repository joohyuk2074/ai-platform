package me.joohyuk.datahub.domain.port.in.listener;

public interface IndexingResponseMessageListener {

  void indexingComplete(IndexingResponse indexingResponse);

  void indexingFailure(IndexingResponse indexingResponse);
}
