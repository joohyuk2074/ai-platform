package me.joohyuk.datahub.application.port.out.external;

import com.spartaecommerce.domain.vo.UserId;
import java.util.Optional;
import me.joohyuk.datahub.domain.entity.User;

public interface UserExternalApiClient {

  Optional<User> getUserById(UserId userId);

  boolean existsUser(UserId userId);

  User createUser(User user);
}
