package me.joohyuk.datarex.domain.port.out.storage;

import java.util.List;
import me.joohyuk.datarex.domain.entity.Passage;

public interface PassageStore {

    void saveAll(List<Passage> passages);
}
