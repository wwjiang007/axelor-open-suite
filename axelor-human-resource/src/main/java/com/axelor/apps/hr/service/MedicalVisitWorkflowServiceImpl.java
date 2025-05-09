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
package com.axelor.apps.hr.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.ICalendarEvent;
import com.axelor.apps.base.db.repo.ICalendarEventRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.ical.ICalendarService;
import com.axelor.apps.hr.db.MedicalVisit;
import com.axelor.apps.hr.db.repo.MedicalVisitRepository;
import com.axelor.apps.hr.exception.HumanResourceExceptionMessage;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.ArrayList;
import java.util.List;

public class MedicalVisitWorkflowServiceImpl implements MedicalVisitWorkflowService {

  protected MedicalVisitRepository medicalVisitRepository;
  protected ICalendarService iCalendarService;
  protected ICalendarEventRepository iCalendarEventRepository;
  protected MedicalVisitService medicalVisitService;

  @Inject
  public MedicalVisitWorkflowServiceImpl(
      MedicalVisitRepository medicalVisitRepository,
      ICalendarService iCalendarService,
      ICalendarEventRepository iCalendarEventRepository,
      MedicalVisitService medicalVisitService) {
    this.medicalVisitRepository = medicalVisitRepository;
    this.iCalendarService = iCalendarService;
    this.iCalendarEventRepository = iCalendarEventRepository;
    this.medicalVisitService = medicalVisitService;
  }

  @Transactional(rollbackOn = {Exception.class})
  @Override
  public void plan(MedicalVisit medicalVisit) throws AxelorException {
    if (medicalVisit.getStatusSelect() != MedicalVisitRepository.STATUS_DRAFT) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(HumanResourceExceptionMessage.MEDICAL_VISIT_PLAN_WRONG_STATUS));
    }

    medicalVisit.setStatusSelect(MedicalVisitRepository.STATUS_PLANNED);
    if (medicalVisit.getEmployee().getUser() != null) {
      createEvent(medicalVisit);
    }
  }

  @Transactional(rollbackOn = {Exception.class})
  @Override
  public void realize(MedicalVisit medicalVisit) throws AxelorException {
    if (medicalVisit.getStatusSelect() != MedicalVisitRepository.STATUS_PLANNED) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(HumanResourceExceptionMessage.MEDICAL_VISIT_REALIZE_WRONG_STATUS));
    }

    medicalVisit.setStatusSelect(MedicalVisitRepository.STATUS_REALIZED);
  }

  @Transactional(rollbackOn = {Exception.class})
  @Override
  public void cancel(MedicalVisit medicalVisit) throws AxelorException {
    checkStatusBeforeCancel(medicalVisit);
    medicalVisit.setStatusSelect(MedicalVisitRepository.STATUS_CANCELED);
    removeEvent(medicalVisit);
  }

  protected void createEvent(MedicalVisit medicalVisit) {
    ICalendarEvent event =
        iCalendarService.createEvent(
            medicalVisit.getMedicalVisitStartDateT(),
            medicalVisit.getMedicalVisitEndDateT(),
            medicalVisit.getEmployee().getUser(),
            medicalVisit.getNote(),
            ICalendarEventRepository.TYPE_EVENT,
            medicalVisitService.getMedicalVisitSubject(medicalVisit));
    iCalendarEventRepository.save(event);
    medicalVisit.setiCalendarEvent(event);
  }

  protected void checkStatusBeforeCancel(MedicalVisit medicalVisit) throws AxelorException {
    List<Integer> authorizedStatus = new ArrayList<>();
    authorizedStatus.add(MedicalVisitRepository.STATUS_PLANNED);
    authorizedStatus.add(MedicalVisitRepository.STATUS_REALIZED);
    Integer status = medicalVisit.getStatusSelect();
    if (status == null || !authorizedStatus.contains(status)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(HumanResourceExceptionMessage.MEDICAL_VISIT_CANCEL_WRONG_STATUS));
    }
  }

  protected void removeEvent(MedicalVisit medicalVisit) {
    iCalendarEventRepository.remove(medicalVisit.getiCalendarEvent());
    medicalVisit.setiCalendarEvent(null);
  }
}
