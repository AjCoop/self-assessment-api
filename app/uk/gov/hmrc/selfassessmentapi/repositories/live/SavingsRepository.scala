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

package uk.gov.hmrc.selfassessmentapi.repositories.live

import play.api.libs.json.Json._
import play.modules.reactivemongo.MongoDbConnection
import reactivemongo.api.DB
import reactivemongo.api.indexes.Index
import reactivemongo.api.indexes.IndexType.Ascending
import reactivemongo.bson.{BSONDocument, BSONObjectID, BSONString}
import uk.gov.hmrc.domain.SaUtr
import uk.gov.hmrc.selfassessmentapi.controllers.api.{SourceId, SummaryId, TaxYear, savings}
import uk.gov.hmrc.mongo.{AtomicUpdate, ReactiveRepository}
import uk.gov.hmrc.mongo.json.ReactiveMongoFormats
import uk.gov.hmrc.selfassessmentapi.controllers.api.savings.Income
import uk.gov.hmrc.selfassessmentapi.repositories.{JsonItem, SourceRepository, SummaryRepository, TypedSourceSummaryRepository}
import uk.gov.hmrc.selfassessmentapi.repositories.domain.{Saving, SavingsIncomeSummary}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object SavingsRepository extends MongoDbConnection {
  private lazy val repository = new SavingsMongoRepository()

  def apply() = repository
}

class SavingsMongoRepository(implicit mongo: () => DB) extends ReactiveRepository[Saving, BSONObjectID](
  "savings",
  mongo,
  domainFormat = Saving.mongoFormats,
  idFormat = ReactiveMongoFormats.objectIdFormats)
  with SourceRepository[savings.Saving] with TypedSourceSummaryRepository[Saving, BSONObjectID] {

  self =>

  override def indexes: Seq[Index] = Seq(
    Index(Seq(("saUtr", Ascending), ("taxYear", Ascending)), name = Some("sav_utr_taxyear"), unique = false),
    Index(Seq(("saUtr", Ascending), ("taxYear", Ascending), ("sourceId", Ascending)), name = Some("sav_utr_taxyear_sourceid"), unique = true),
    Index(Seq(("saUtr", Ascending), ("taxYear", Ascending), ("sourceId", Ascending), ("incomes.summaryId", Ascending)), name = Some("sav_utr_taxyear_source_incomesid"), unique = true),
    Index(Seq(("lastModifiedDateTime", Ascending)), name = Some("sav_last_modified"), unique = false)
  )

  override def create(saUtr: SaUtr, taxYear: TaxYear, saving: savings.Saving) = {
    val mongoSav = Saving.create(saUtr, taxYear, saving)
    insert(mongoSav).map(_ => mongoSav.sourceId)
  }

  override def findById(saUtr: SaUtr, taxYear: TaxYear, id: SourceId) = {
    for(option <- findMongoObjectById(saUtr, taxYear, id)) yield option.map(_.toSavings)
  }

  override def list(saUtr: SaUtr, taxYear: TaxYear) = {
    for (list <- find("saUtr" -> saUtr.utr, "taxYear" -> taxYear.taxYear)) yield list.map(_.toSavings)
  }

  def findAll(saUtr: SaUtr, taxYear: TaxYear): Future[Seq[Saving]] = {
    find("saUtr" -> saUtr.utr, "taxYear" -> taxYear.taxYear)
  }

  override def listAsJsonItem(saUtr: SaUtr, taxYear: TaxYear) =
    list(saUtr, taxYear).map(_.map(sav => JsonItem(sav.id.get.toString, toJson(sav))))

  /*
  We need to perform updates manually as we are using one collection per source and it includes the arrays of summaries. This
  update is however partial so we should only update the fields provided and not override the summary arrays.
 */
  override def update(saUtr: SaUtr, taxYear: TaxYear, id: SourceId, saving: savings.Saving) = {
    val modifiers = BSONDocument(Seq(modifierStatementLastModified))
    for {
      result <- atomicUpdate(
        BSONDocument("saUtr" -> BSONString(saUtr.toString), "taxYear" -> BSONString(taxYear.toString), "sourceId" -> BSONString(id)),
        modifiers
      )
    } yield result.nonEmpty
  }

  object IncomeRepository extends SummaryRepository[Income] {

    override def create(saUtr: SaUtr, taxYear: TaxYear, sourceId: SourceId, summary: Income): Future[Option[SummaryId]] = {
      self.createSummary(saUtr, taxYear, sourceId, SavingsIncomeSummary.toMongoSummary(summary))
    }

    override def findById(saUtr: SaUtr, taxYear: TaxYear, sourceId: SourceId, id: SummaryId): Future[Option[Income]] = {
      self.findSummaryById(saUtr, taxYear, sourceId, _.incomes.find(_.summaryId == id).map(_.toSavingsIncome))
    }

    override def update(saUtr: SaUtr, taxYear: TaxYear, sourceId: SourceId, id: SummaryId, summary: Income): Future[Boolean] = {
      self.updateSummary(saUtr, taxYear, sourceId, SavingsIncomeSummary.toMongoSummary(summary), _.incomes.exists(_.summaryId == id))
    }

    override def delete(saUtr: SaUtr, taxYear: TaxYear, sourceId: SourceId, id: SummaryId): Future[Boolean] = {
      self.deleteSummary(saUtr, taxYear, sourceId, id, SavingsIncomeSummary.arrayName, _.incomes.exists(_.summaryId == id))
    }

    override def list(saUtr: SaUtr, taxYear: TaxYear, sourceId: SourceId): Future[Option[Seq[Income]]] = {
      self.listSummaries(saUtr, taxYear, sourceId, _.incomes.map(_.toSavingsIncome))
    }

    override def listAsJsonItem(saUtr: SaUtr, taxYear: TaxYear, sourceId: SourceId): Future[Seq[JsonItem]] = {
      list(saUtr, taxYear, sourceId).map(_.getOrElse(Seq()).map(income => JsonItem(income.id.get.toString, toJson(income))))
    }
  }
}
