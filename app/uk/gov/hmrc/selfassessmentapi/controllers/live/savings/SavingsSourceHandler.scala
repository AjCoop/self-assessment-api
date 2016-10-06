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

package uk.gov.hmrc.selfassessmentapi.controllers.live.savings

import uk.gov.hmrc.play.http.NotImplementedException
import uk.gov.hmrc.selfassessmentapi.controllers.{SourceHandler, SummaryHandler}
import uk.gov.hmrc.selfassessmentapi.controllers.api.SummaryType
import uk.gov.hmrc.selfassessmentapi.controllers.api.savings.{Income, Saving}
import uk.gov.hmrc.selfassessmentapi.controllers.api.savings.SourceType.Savings
import uk.gov.hmrc.selfassessmentapi.controllers.api.savings.SummaryTypes.Incomes
import uk.gov.hmrc.selfassessmentapi.repositories.live.SavingsRepository
import uk.gov.hmrc.selfassessmentapi.repositories.{SourceRepositoryWrapper, SummaryRepositoryWrapper}

object SavingsSourceHandler extends SourceHandler(Saving, Savings.name) {
  override val repository = SourceRepositoryWrapper(SavingsRepository())

  override def summaryHandler(summaryType: SummaryType): Option[SummaryHandler[_]] = {
    summaryType match {
      case Incomes => Some(SummaryHandler(SummaryRepositoryWrapper(SavingsRepository().IncomeRepository), Income, Incomes.name))
      case _ => throw new NotImplementedException(s"${Savings.name} ${summaryType.name} is not implemented")
    }
  }
}
