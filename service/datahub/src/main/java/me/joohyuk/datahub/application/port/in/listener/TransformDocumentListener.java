package me.joohyuk.datahub.application.port.in.listener;

import me.joohyuk.datahub.domain.entity.PassageResponse;

public interface TransformDocumentListener {

  void onCompleted(PassageResponse passageResponse);

  void onFailed(PassageResponse passageResponse);
}
