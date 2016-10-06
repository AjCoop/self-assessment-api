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

package uk.gov.hmrc.selfassessmentapi.repositories.domain

import org.joda.time.{DateTime, DateTimeZone}
import play.api.libs.json.{Format, Json}
import reactivemongo.bson.{BSONDocument, BSONDouble, BSONObjectID, BSONString}
import uk.gov.hmrc.domain.SaUtr
import uk.gov.hmrc.mongo.json.ReactiveMongoFormats
import uk.gov.hmrc.selfassessmentapi.controllers.api.{TaxYear, _}
import uk.gov.hmrc.selfassessmentapi.controllers.api.savings.{Saving => ApiSaving}
import uk.gov.hmrc.selfassessmentapi.controllers.api.savings.{Income, IncomeType}
import uk.gov.hmrc.selfassessmentapi.controllers.api.savings.IncomeType.IncomeType

case class SavingsIncomeSummary(summaryId: SummaryId, `type`: IncomeType, amount: BigDecimal) extends Summary {
  override val arrayName: String = SavingsIncomeSummary.arrayName

  def toSavingsIncome = Income(Some(summaryId), `type`, amount)

  def toBsonDocument = BSONDocument(
    "summaryId" -> summaryId,
    "type" -> BSONString(`type`.toString),
    "amount" -> BSONDouble(amount.doubleValue()))
}

object SavingsIncomeSummary {

  val arrayName = "savings"

  implicit val format = Json.format[SavingsIncomeSummary]

  def toMongoSummary(income: Income, id: Option[SummaryId] = None): SavingsIncomeSummary = {
    SavingsIncomeSummary(
      summaryId = id.getOrElse(BSONObjectID.generate.stringify),
      `type` = income.`type`,
      amount = income.amount
    )
  }
}

case class Saving(id: BSONObjectID,
                  sourceId: SourceId,
                  saUtr: SaUtr,
                  taxYear: TaxYear,
                  lastModifiedDateTime: DateTime,
                  createdDateTime: DateTime,
                  incomes: Seq[SavingsIncomeSummary] = Seq.empty) extends SourceMetadata {

  def toSavings = savings.Saving(Some(sourceId))

  def taxedSavingsInterest = incomes.filter(_.`type` == IncomeType.InterestFromBanksTaxed).map(_.amount).sum
}

object Saving {
  implicit val dateTimeFormat = ReactiveMongoFormats.dateTimeFormats
  implicit val localDateFormat = ReactiveMongoFormats.localDateFormats

  implicit val mongoFormats = ReactiveMongoFormats.mongoEntity({
    implicit val BSONObjectIDFormat: Format[BSONObjectID] = ReactiveMongoFormats.objectIdFormats
    implicit val dateTimeFormat: Format[DateTime] = ReactiveMongoFormats.dateTimeFormats
    Format(Json.reads[Saving], Json.writes[Saving])
  })

  def create(saUtr: SaUtr, taxYear: TaxYear, savings: ApiSaving) = {
    val id = BSONObjectID.generate
    val now = DateTime.now(DateTimeZone.UTC)

    Saving(id, id.stringify, saUtr, taxYear, now, now)
  }
}
