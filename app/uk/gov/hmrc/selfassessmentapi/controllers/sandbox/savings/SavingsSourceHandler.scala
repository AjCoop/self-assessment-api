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

package uk.gov.hmrc.selfassessmentapi.controllers.sandbox.savings

import play.api.libs.json.Writes
import uk.gov.hmrc.selfassessmentapi.controllers.api.savings.SourceType.Savings
import uk.gov.hmrc.selfassessmentapi.controllers.api.savings.SummaryTypes.Incomes
import uk.gov.hmrc.selfassessmentapi.controllers.api.savings.{Income, Saving}
import uk.gov.hmrc.selfassessmentapi.controllers.api.{SummaryId, SummaryType}
import uk.gov.hmrc.selfassessmentapi.controllers.{SourceHandler, SummaryHandler}
import uk.gov.hmrc.selfassessmentapi.repositories.sandbox.{SandboxSourceRepository, SandboxSummaryRepository}

class SavingsSourceHandler extends SourceHandler(Saving, Savings.name) {
  override val repository = new SandboxSourceRepository[Saving] {
    override def example(id: SummaryId): Saving = Saving.example(Some(id))

    override implicit val writes: Writes[Saving] = Saving.writes
  }

  override def summaryHandler(summaryType: SummaryType): Option[SummaryHandler[_]] = {
    summaryType match {
      case Incomes => Some(SummaryHandler(new SandboxSummaryRepository[Income] {
        override def example(id: Option[SummaryId]): Income = Income.example(id)

        override implicit val writes: Writes[Income] = Income.writes
      }, Income, Incomes.name))
      case _ => None
    }
  }
}
