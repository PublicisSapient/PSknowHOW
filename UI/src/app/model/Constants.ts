/*******************************************************************************
 * Copyright 2014 CapitalOne, LLC.
 * Further development Copyright 2022 Sapient Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

export class Constants {
  public static INSTANCE_OF_KPI_SCORE = 'kpi_score_instance';
  public static INSTANCE_OF_KPI_QUESTIONNAIRE = 'kpi_questiionnaire_instance';

  public static FIELD_TYPE_TEXT = 'text';
  public static FIELD_TYPE_DROPDOWN = 'dropdown';
  public static FIELD_TYPE_HIDDEN = 'hidden';
  public static FIELD_TYPE_CHECKBOX = 'checkbox';
  public static FIELD_TYPE_RADIO = 'radio';

  public static FIELD_NAME_SEPARATOR = '_';
}

export const KPI_HEADER_ACTION = {
  setting: false,
  listView: false,
  explore: false,
  comment: false,
};
