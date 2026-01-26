package com.spartaecommerce.domain.vo;

import com.spartaecommerce.exception.BusinessException;
import com.spartaecommerce.exception.ErrorCode;
import java.time.LocalDateTime;

public record DateRange(
    LocalDateTime startDate,
    LocalDateTime endDate
) {

  public DateRange {
    if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
      throw new BusinessException(ErrorCode.INVALID_REQUEST,
          "Start date must be before or equal to end date");
    }
  }

  public static DateRange of(LocalDateTime startDate, LocalDateTime endDate) {
    return new DateRange(startDate, endDate);
  }

  public boolean hasStartDate() {
    return startDate != null;
  }

  public boolean hasEndDate() {
    return endDate != null;
  }
}