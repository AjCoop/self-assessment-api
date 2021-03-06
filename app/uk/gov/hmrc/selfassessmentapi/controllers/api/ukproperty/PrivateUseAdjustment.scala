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

package uk.gov.hmrc.selfassessmentapi.controllers.api.ukproperty

import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._
import uk.gov.hmrc.selfassessmentapi.controllers.api._

case class PrivateUseAdjustment(id: Option[SummaryId] = None,
                           amount: BigDecimal)

object PrivateUseAdjustment extends JsonMarshaller[PrivateUseAdjustment] {

  implicit val writes = Json.writes[PrivateUseAdjustment]
  implicit val reads: Reads[PrivateUseAdjustment] = (
    Reads.pure(None) and
      (__ \ "amount").read[BigDecimal](positiveAmountValidator("amount"))
    ) (PrivateUseAdjustment.apply _)

  override def example(id: Option[SummaryId]) = PrivateUseAdjustment(id, BigDecimal(1000))
}
