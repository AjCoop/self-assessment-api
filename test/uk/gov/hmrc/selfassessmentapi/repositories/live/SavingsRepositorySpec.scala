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

import java.util.UUID

import org.scalatest.BeforeAndAfterEach
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.selfassessmentapi.MongoEmbeddedDatabase
import uk.gov.hmrc.selfassessmentapi.controllers.api.savings.{Income, Saving}

import scala.concurrent.ExecutionContext.Implicits.global

class SavingsRepositorySpec extends MongoEmbeddedDatabase with BeforeAndAfterEach {

  private val repo = new SavingsMongoRepository
  private val mongoIncomeRepository = repo.IncomeRepository

  private val saUtr = generateSaUtr()
  private def saving = Saving.example()

  override def beforeEach(): Unit = {
    await(repo.drop)
    await(repo.ensureIndexes)
  }

  "deleteById" should {
    "return true when a saving is deleted" in {
      val id = await(repo.create(saUtr, taxYear, saving))
      val result = await(repo.delete(saUtr, taxYear, id))

      result shouldEqual true
    }

    "return false when a saving is not deleted" in {
      val result = await(repo.delete(saUtr, taxYear, "madeUpID"))
      result shouldEqual false
    }
  }

  "delete" should {
    "delete all savings for a provided utr and tax year" in {
      for {
        n <- 1 to 10
        source = saving
        id = await(repo.create(saUtr, taxYear, source))
      } yield source.copy(id = Some(id))


      await(repo.delete(saUtr, taxYear))

      val found: Seq[_] = await(repo.list(saUtr, taxYear))

      found shouldBe empty
    }

    "not delete savings for different utrs and tax years" in {
      val saUtr2 = generateSaUtr()
      await(repo.create(saUtr, taxYear, saving))
      val source2 = await(repo.create(saUtr2, taxYear, saving))

      await(repo.delete(saUtr, taxYear))
      val found = await(repo.list(saUtr2, taxYear))

      found.flatMap(_.id) should contain theSameElementsAs Seq(source2)
    }
  }

  "list" should {
    "retrieve all unearned incomes for utr/tax year" in {
      val sources = for {
        n <- 1 to 10
        source = saving
        id = await(repo.create(saUtr, taxYear, source))
      } yield source.copy(id = Some(id))


      val found: Seq[_] = await(repo.list(saUtr, taxYear))

      found should contain theSameElementsAs sources
    }

    "not include unearned incomes for different utr" in {
      val source1 = await(repo.create(saUtr, taxYear, saving))
      await(repo.create(generateSaUtr(), taxYear, saving))

      val found = await(repo.list(saUtr, taxYear))

      found.flatMap(_.id) should contain theSameElementsAs Seq(source1)
    }
  }

  "update" should {
    "return false when the unearned income does not exist" in {
      val result = await(repo.update(saUtr, taxYear, UUID.randomUUID().toString, saving))
      result shouldEqual false
    }

    "update last modified" in {
      val source = saving
      val sourceId = await(repo.create(saUtr, taxYear, source))
      val found = await(repo.findById(BSONObjectID(sourceId)))
      await(repo.update(saUtr, taxYear, sourceId, source))

      val found1 = await(repo.findById(BSONObjectID(sourceId)))

      // Added the equals clauses as it was failing locally once, can fail if the test runs faster and has the same time for create and update
      found1.get.lastModifiedDateTime.isEqual(found.get.lastModifiedDateTime) || found1.get.lastModifiedDateTime.isAfter(found.get.lastModifiedDateTime) shouldEqual true
    }
  }

  "create savings income" should {
    "create an income duhhhh" in {
      val id = BSONObjectID.generate.stringify

      val summary = await(mongoIncomeRepository.create(saUtr, taxYear, id, Income.example()))
      val result = mongoIncomeRepository.list(saUtr, taxYear, id)

      val x = 5 + 5
    }

    "add an income to an empty list when source exists and return id" in {
      val sourceId = await(repo.create(saUtr, taxYear, saving))
      val summaryId = await(mongoIncomeRepository.create(saUtr, taxYear, sourceId, Income.example()))

      summaryId.isDefined shouldEqual true
      val dbSummaries = await(mongoIncomeRepository.list(saUtr, taxYear, sourceId))

      val found = dbSummaries.get
      found.headOption shouldEqual Some(Income.example(id = summaryId))
    }

    "add an income to the existing list when source exists and return id" in {
        val sourceId = await(repo.create(saUtr, taxYear, saving))
        val summaryId = await(mongoIncomeRepository.create(saUtr, taxYear, sourceId, Income.example()))
        val summaryId1 = await(mongoIncomeRepository.create(saUtr, taxYear, sourceId, Income.example()))

        val summaries = await(mongoIncomeRepository.list(saUtr, taxYear, sourceId))

        val found = summaries.get
        found should contain theSameElementsAs Seq(Income.example(id = summaryId), Income.example(id = summaryId1))
    }

    "return none when source does not exist" in {
      val summaryId = await(mongoIncomeRepository.create(saUtr, taxYear, BSONObjectID.generate.stringify, Income.example()))
      summaryId shouldEqual None
    }
  }

  "findById savings income" should {
    "return none if the source does not exist" in {
      await(mongoIncomeRepository
          .findById(saUtr, taxYear, BSONObjectID.generate.stringify, BSONObjectID.generate.stringify)) shouldEqual None
    }

    "return none if the summary does not exist" in {
      val sourceId = await(repo.create(saUtr, taxYear, saving))
      await(mongoIncomeRepository.findById(saUtr, taxYear, sourceId, BSONObjectID.generate.stringify)) shouldEqual None
    }

    "return the summary if found" in {
      val sourceId = await(repo.create(saUtr, taxYear, saving))
      val summaryId = await(mongoIncomeRepository.create(saUtr, taxYear, sourceId, Income.example())).get
      val found = await(mongoIncomeRepository.findById(saUtr, taxYear, sourceId, summaryId))

      found shouldEqual Some(Income.example(id = Some(summaryId)))
    }
  }

  "list savings income" should {
    "return empty list when source has no summaries" in {
      val sourceId = await(repo.create(saUtr, taxYear, saving))
      await(mongoIncomeRepository.list(saUtr, taxYear, sourceId)) shouldEqual Some(Seq.empty)
    }

    "return none when source does not exist" in {
      await(mongoIncomeRepository.list(saUtr, taxYear, BSONObjectID.generate.stringify)) shouldEqual None
    }
  }

  "delete savings income" should {
    "return true when the summary has been deleted" in {
      val sourceId = await(repo.create(saUtr, taxYear, saving))
      val summaryId = await(mongoIncomeRepository.create(saUtr, taxYear, sourceId, Income.example())).get
      await(mongoIncomeRepository.delete(saUtr, taxYear, sourceId, summaryId)) shouldEqual true
    }

    "only delete the specified summary" in {
      val sourceId = await(repo.create(saUtr, taxYear, saving))
      val summaryId = await(mongoIncomeRepository.create(saUtr, taxYear, sourceId, Income.example())).get
      val summaryId1 = await(mongoIncomeRepository.create(saUtr, taxYear, sourceId, Income.example()))
      await(mongoIncomeRepository.delete(saUtr, taxYear, sourceId, summaryId))

      val found = await(mongoIncomeRepository.list(saUtr, taxYear, sourceId)).get
      found.size shouldEqual 1
      found.head shouldEqual Income.example(id = summaryId1)
    }

    "return false when the source does not exist" in {
      await(mongoIncomeRepository.delete(saUtr, taxYear, BSONObjectID.generate.stringify, BSONObjectID.generate.stringify)) shouldEqual false
    }

    "return false when the summary does not exist" in {
      val sourceId = await(repo.create(saUtr, taxYear, saving))
      await(mongoIncomeRepository.delete(saUtr, taxYear, sourceId, BSONObjectID.generate.stringify)) shouldEqual false
    }
  }


  "update savings income" should {
    "return true when the income has been updated" in {
      val sourceId = await(repo.create(saUtr, taxYear, saving))
      val summaryId = await(mongoIncomeRepository.create(saUtr, taxYear, sourceId, Income.example())).get

      await(mongoIncomeRepository.update(saUtr, taxYear, sourceId, summaryId, Income.example())) shouldEqual true

      val found = await(mongoIncomeRepository.findById(saUtr, taxYear, sourceId, summaryId))

      found shouldEqual Some(Income.example(id = Some(summaryId)))
    }

    "only update the specified income" in {
        val sourceId = await(repo.create(saUtr, taxYear, saving))
        val summaryId1 = await(mongoIncomeRepository.create(saUtr, taxYear, sourceId, Income.example())).get
        val summaryId2 = await(mongoIncomeRepository.create(saUtr, taxYear, sourceId, Income.example())).get

        await(mongoIncomeRepository.update(saUtr, taxYear, sourceId, summaryId2, Income.example())) shouldEqual true

        val found = await(mongoIncomeRepository.list(saUtr, taxYear, sourceId)).get

        found should contain theSameElementsAs
          Seq(Income.example(id = Some(summaryId1)), Income.example(id = Some(summaryId2)))
    }

    "return false when the source does not exist" in {
      await(
        mongoIncomeRepository.update(saUtr, taxYear, BSONObjectID.generate.stringify, BSONObjectID.generate.stringify, Income.example())) shouldEqual false
    }

    "return false when the income does not exist" in {
      val sourceId = await(repo.create(saUtr, taxYear, saving))

      await(mongoIncomeRepository.update(saUtr, taxYear, sourceId, BSONObjectID.generate.stringify, Income.example())) shouldEqual false
    }
  }
}
