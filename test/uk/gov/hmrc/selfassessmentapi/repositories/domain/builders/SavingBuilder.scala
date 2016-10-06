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

package uk.gov.hmrc.selfassessmentapi.repositories.domain.builders

import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.selfassessmentapi.repositories.domain.{Saving, SavingsIncomeSummary}
import uk.gov.hmrc.selfassessmentapi.TestUtils._
import uk.gov.hmrc.selfassessmentapi.controllers.api.savings.IncomeType._

case class SavingBuilder(id: BSONObjectID = BSONObjectID.generate) {
  private var saving = Saving(id, id.stringify, generateSaUtr(), taxYear, now, now)

  private def withSavings(savings: (IncomeType, BigDecimal)*) = {
    saving = saving.copy(incomes = saving.incomes ++ savings.map(saving => SavingsIncomeSummary("", saving._1, saving._2)))
    this
  }

  def withTaxedIncome(savings : BigDecimal*) = {
    withSavings(savings.map((InterestFromBanksTaxed, _)):_*)
  }

  def withUntaxedIncome(savings : BigDecimal*) = {
    withSavings(savings.map((InterestFromBanksUntaxed, _)):_*)
  }

  def create() = saving
}
