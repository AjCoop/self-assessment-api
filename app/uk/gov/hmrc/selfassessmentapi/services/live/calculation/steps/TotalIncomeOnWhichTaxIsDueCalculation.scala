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

package uk.gov.hmrc.selfassessmentapi.services.live.calculation.steps

import uk.gov.hmrc.selfassessmentapi.repositories.domain.MongoLiability

object TotalIncomeOnWhichTaxIsDueCalculation extends CalculationStep {

  override def run(selfAssessment: SelfAssessment, liability: MongoLiability): MongoLiability = {

    val totalIncomeReceived = liability.totalIncomeReceived.getOrElse(throw new IllegalStateException("TotalIncomeOnWhichTaxIsDueCalculation cannot be performed because totalIncomeReceived has not been calculated"))

    val totalAllowancesAndReliefs = liability.totalAllowancesAndReliefs.getOrElse(throw new IllegalStateException("TotalIncomeOnWhichTaxIsDueCalculation cannot be performed because totalAllowancesAndReliefs has not been calculated"))

    val totalIncomeOnWhichTaxIsDue = positiveOrZero(totalIncomeReceived - totalAllowancesAndReliefs)

    liability.copy(totalIncomeOnWhichTaxIsDue = Some(totalIncomeOnWhichTaxIsDue))
  }
}