/*
 * Copyright 2016 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.selfassessmentapi.controllers.api.savings

import play.api.libs.json.{ Json, JsValue }
import uk.gov.hmrc.selfassessmentapi.controllers.api.{FullFieldDescription, PositiveMonetaryFieldDescription, SummaryId, SummaryType}

object SummaryTypes {
  case object Incomes extends SummaryType {
    override val name = "incomes"
    override val documentationName = "Incomes"

    override def example(id: Option[SummaryId]): JsValue = Json.toJson(Income.example(id))

    override val title = "Sample savings incomes"

    override def description(action: String): String = s"$action a saving income for the specified source"

    override val fieldDescriptions = Seq(
      FullFieldDescription("savings", "type", "Enum", s"Type of savings income (one of the following: ${IncomeType.values.mkString(", ")})"),
      PositiveMonetaryFieldDescription("savings", "amount", "Interest income from UK banks and building societies, split by interest types - Taxed interest and Untaxed interest.")
    )
  }
}
