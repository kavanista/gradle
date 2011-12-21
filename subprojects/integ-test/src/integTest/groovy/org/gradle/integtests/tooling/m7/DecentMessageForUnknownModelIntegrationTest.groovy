/*
 * Copyright 2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.integtests.tooling.m7

import org.gradle.integtests.tooling.fixture.MinTargetGradleVersion
import org.gradle.integtests.tooling.fixture.MinToolingApiVersion
import org.gradle.integtests.tooling.fixture.ToolingApiSpecification
import org.gradle.tooling.UnknownModelException
import org.gradle.tooling.UnsupportedVersionException

@MinToolingApiVersion('1.0-milestone-7')
@MinTargetGradleVersion('1.0-milestone-7')
class DecentMessageForUnknownModelIntegrationTest extends ToolingApiSpecification {

    def setup() {
        //for debugging purposes:
        toolingApi.isEmbedded = false
    }

    class UnknownModel {}

    def "fails gracefully when building unknown model"() {
        when:
        def e = maybeFailWithConnection { it.getModel(UnknownModel.class) }

        then:
        e instanceof UnsupportedVersionException //backwards compatibility
        e instanceof UnknownModelException
    }
}