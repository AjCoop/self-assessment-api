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

package uk.gov.hmrc.selfassessmentapi.controllers

import play.api.mvc.PathBindable
import uk.gov.hmrc.domain.SaUtr
import uk.gov.hmrc.referencechecker.SelfAssessmentReferenceChecker
import uk.gov.hmrc.selfassessmentapi.config.AppContext
import uk.gov.hmrc.selfassessmentapi.controllers.api.{SourceType, SourceTypes, TaxYear}

object Binders {

  implicit def saUtrBinder(implicit stringBinder: PathBindable[String]) = new PathBindable[SaUtr] {

    def unbind(key: String, saUtr: SaUtr): String = stringBinder.unbind(key, saUtr.value)

    def bind(key: String, value: String): Either[String, SaUtr] = {
      SelfAssessmentReferenceChecker.isValid(value) match {
        case true => Right(SaUtr(value))
        case false => Left("ERROR_SA_UTR_INVALID")
      }
    }
  }

  implicit def taxYearBinder(implicit stringBinder: PathBindable[String]) = new PathBindable[TaxYear] {

    def unbind(key: String, taxYear: TaxYear): String = stringBinder.unbind(key, taxYear.value)

    def bind(key: String, value: String): Either[String, TaxYear] = {
      AppContext.supportedTaxYears.contains(value) match {
        case true => Right(TaxYear(value))
        case false => Left("ERROR_TAX_YEAR_INVALID")
      }
    }
  }

  implicit def sourceTypeBinder(implicit stringBinder: PathBindable[String]) = new PathBindable[SourceType] {

    def unbind(key: String, `type`: SourceType): String = `type`.name

    def bind(key: String, value: String): Either[String, SourceType] = {
      SourceTypes.fromName(value.toLowerCase) match {
        case Some(v) => Right(v)
        case None => Left("ERROR_INVALID_SOURCE_TYPE")
      }
    }
  }
}
