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

import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.selfassessmentapi.controllers.api._
import uk.gov.hmrc.selfassessmentapi.controllers.api.savings.SummaryTypes.Incomes

object SourceType {
  object Savings extends SourceType {
    override val name = "savings"
    override val documentationName = "Savings"

    override def example(id: Option[SummaryId] = None): JsValue = Json.toJson(Savings.example(id))

    override val summaryTypes: Set[SummaryType] = Set(Incomes)
    override val title = "Sample saving"

    override def description(action: String) = s"$action a saving"
    override val fieldDescriptions = Seq()
  }
}
