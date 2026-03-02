package com.spartaecommerce.util;

import java.time.Instant;
import java.time.LocalDateTime;

public interface DateTimeHolder {

    LocalDateTime getCurrentDateTime();

    Instant now();
}
