/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */

package jenkins.tests

import org.junit.*
import com.lesfurets.jenkins.unit.*
import static groovy.test.GroovyAssert.*

class TestBuildFailureMessage extends BasePipelineTestCPS {

    def buildFailureMessage

    @Before
    void setUp() {
        super.setUp()
        buildFailureMessage = loadScript("../../vars/buildFailureMessage.groovy")
    }

    @Test
    void testCall() {
        def result = buildFailureMessage()
        assertEquals "result:", result
    }


}
