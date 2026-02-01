package me.joohyuk.datahub.domain.port.in.listener;

import me.joohyuk.datahub.domain.entity.PassageResponse;

public interface PassageCreationResultListener {

  void onCompleted(PassageResponse passageResponse);

  void onFailed(PassageResponse passageResponse);
}
