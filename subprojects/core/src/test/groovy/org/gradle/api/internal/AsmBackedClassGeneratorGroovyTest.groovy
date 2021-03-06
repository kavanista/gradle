/*
 * Copyright 2012 the original author or authors.
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

package org.gradle.api.internal

import org.gradle.internal.reflect.DirectInstantiator
import spock.lang.Specification
import spock.lang.Issue
import org.gradle.util.ConfigureUtil

class AsmBackedClassGeneratorGroovyTest extends Specification {

    def generator = new AsmBackedClassGenerator()
    def instantiator = new ClassGeneratorBackedInstantiator(generator, new DirectInstantiator())

    private <T> T create(Class<T> clazz, Object... args) {
        instantiator.newInstance(clazz, *args)
    }

    @Issue("GRADLE-2417")
    def "can use dynamic object as closure delegate"() {
        given:
        def thing = create(DynamicThing)

        when:
        conf(thing) {
            m1(1,2,3)
            p1 = 1
            p1 = p1 + 1
        }

        then:
        thing.methods.size() == 1
        thing.props.p1 == 2
    }

    def "unassociated missing exceptions are thrown"() {
        given:
        def thing1 = create(DynamicThing)

        when:
        thing1.onMethodMissing = { name, args -> [].foo() }
        conf(thing1) { m1() }

        then:
        def e = thrown(groovy.lang.MissingMethodException)
        e.method == "foo"

        when:
        thing1.onPropertyMissingGet = { new Object().bar }
        conf(thing1) { abc }

        then:
        e = thrown(groovy.lang.MissingPropertyException)
        e.property == "bar"

        when:
        thing1.onPropertyMissingSet = { name, value -> new Object().baz = true }
        conf(thing1) { abc = true }

        then:
        e = thrown(groovy.lang.MissingPropertyException)
        e.property == "baz"

    }


    def conf(o, c) {
        ConfigureUtil.configure(c, o)
    }
}

class DynamicThing {
    def methods = [:]
    def props = [:]

    Closure onMethodMissing = { name, args -> methods[name] = args.toList() }
    Closure onPropertyMissingGet = { name -> props[name] }
    Closure onPropertyMissingSet = { name, value -> props[name] = value }

    def methodMissing(String name, args) {
        onMethodMissing(name, args)
    }

    def propertyMissing(String name) {
        onPropertyMissingGet(name)
    }

    def propertyMissing(String name, value) {
        onPropertyMissingSet(name, value)
    }
}