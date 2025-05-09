/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.axelor.apps.hr.rest.dto;

import com.axelor.apps.hr.db.LeaveReason;
import com.axelor.utils.api.ObjectFinder;
import com.axelor.utils.api.RequestPostStructure;
import java.time.LocalDate;

public class LeaveDaysToDatePostRequest extends RequestPostStructure {

  private LocalDate toDate;

  private Long leaveReasonId;

  public LocalDate getToDate() {
    return toDate;
  }

  public void setToDate(LocalDate toDate) {
    this.toDate = toDate;
  }

  public Long getLeaveReasonId() {
    return leaveReasonId;
  }

  public void setLeaveReasonId(Long leaveReasonId) {
    this.leaveReasonId = leaveReasonId;
  }

  public LeaveReason fetchLeaveReason() {
    if (leaveReasonId == null || leaveReasonId == 0L) {
      return null;
    }
    return ObjectFinder.find(LeaveReason.class, leaveReasonId, ObjectFinder.NO_VERSION);
  }
}
