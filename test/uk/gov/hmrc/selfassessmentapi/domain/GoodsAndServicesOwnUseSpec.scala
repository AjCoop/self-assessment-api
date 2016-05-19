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

package uk.gov.hmrc.selfassessmentapi.domain
import ErrorCode._

class GoodsAndServicesOwnUseSpec extends JsonSpec {

  "format" should {
    "round trip GoodsAndServicesOwnUse json" in {
      roundTripJson(GoodsAndServicesOwnUse(amount = BigDecimal(1000)))
    }
  }

  "validate" should {
    "reject amounts with more than 2 decimal values" in {
      Seq(BigDecimal(1000.123), BigDecimal(1000.12456), BigDecimal(1000.123454), BigDecimal(1000.123456789)).foreach { testAmount =>
        val goodsAndServiceOwnUseId = GoodsAndServicesOwnUse(amount = testAmount)
        assertValidationError[GoodsAndServicesOwnUse](
          goodsAndServiceOwnUseId,
          Map(INVALID_MONETARY_AMOUNT -> "amount should be non-negative number up to 2 decimal values"),
          "Expected invalid self-employment-goods-and-services-for-own-use")
      }
    }

    "reject negative amount" in {
      val seExpense = GoodsAndServicesOwnUse(amount = BigDecimal(-1000.12))
      assertValidationError[GoodsAndServicesOwnUse](
        seExpense,
        Map(INVALID_MONETARY_AMOUNT -> "amount should be non-negative number up to 2 decimal values"),
        "Expected negative self-employment-goods-and-services-for-own-use")
    }
  }
}