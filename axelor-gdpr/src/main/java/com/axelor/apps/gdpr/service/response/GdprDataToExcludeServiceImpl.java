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
package com.axelor.apps.gdpr.service.response;

import com.axelor.apps.gdpr.db.GDPRDataToExcludeConfig;
import com.axelor.meta.db.MetaField;
import com.axelor.meta.db.MetaModel;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class GdprDataToExcludeServiceImpl implements GdprDataToExcludeService {

  @Override
  public boolean isModelExcluded(
      List<GDPRDataToExcludeConfig> dataToExcludeConfig, MetaModel model) {
    return dataToExcludeConfig.stream()
        .map(GDPRDataToExcludeConfig::getMetaModel)
        .anyMatch(metaModel -> metaModel.equals(model));
  }

  @Override
  public List<MetaField> getFieldsToExclude(
      List<GDPRDataToExcludeConfig> dataToExcludeConfig, MetaModel model) {
    return dataToExcludeConfig.stream()
        .filter(gdprDataToExcludeConfig -> gdprDataToExcludeConfig.getMetaModel().equals(model))
        .map(GDPRDataToExcludeConfig::getMetaFields)
        .flatMap(Collection::stream)
        .collect(Collectors.toList());
  }
}
