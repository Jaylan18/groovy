/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.groovy.ginq.provider.collection.runtime

import groovy.transform.CompileStatic

import java.util.stream.Collectors

import static org.apache.groovy.ginq.provider.collection.runtime.Queryable.from

/**
 * Helper for {@link Queryable}
 *
 * @since 4.0.0
 */
@CompileStatic
class QueryableHelper {
    /**
     * Make {@link Queryable} instance's data source records being able to access via aliases
     *
     * @param queryable the original {@link Queryable} instance
     * @param aliasList the aliases of clause {@code from} and joins
     * @return the result {@link Queryable} instance
     * @since 4.0.0
     */
    static <T> Queryable<SourceRecord<T>> navigate(Queryable<? extends T> queryable, List<String> aliasList) {
        List<SourceRecord<T>> sourceRecordList =
                queryable.stream()
                        .map(e -> new SourceRecord<T>(e, aliasList))
                        .collect(Collectors.toList())

        return from(sourceRecordList)
    }

    /**
     * Returns single value of {@link Queryable} instance
     *
     * @param queryable the {@link Queryable} instance
     * @return the single value
     * @throws TooManyValuesException if the {@link Queryable} instance contains more than one value
     * @since 4.0.0
     */
    static <T> T singleValue(final Queryable<? extends T> queryable) {
        List<? extends T> list = queryable.toList()
        int size = list.size()

        if (0 == size) {
            return null
        }
        if (1 == size) {
            return list.get(0)
        }

        throw new TooManyValuesException("subquery returns more than one value: $list")
    }

    /**
     * Check if two objects are identical
     *
     * @param obj1 object to check
     * @param obj2 the other object to check
     * @return the result
     * @since 4.0.0
     */
    static boolean isIdentical(Object obj1, Object obj2) {
        if (obj1 === obj2) return true
        if (null === obj1 || null === obj2) return false
        if (obj1.getClass() !== obj2.getClass()) return false

        if (obj1 instanceof NamedTuple && obj2 instanceof NamedTuple) {
            def obj1Size = obj1.size()
            if (obj1Size == obj2.size()) {
                for (int i = 0; i < obj1Size; i++) {
                    if (!isIdentical(obj1.get(i), obj2.get(i))) {
                        return false
                    }
                }
                return true
            }

            return false
        }

        return obj1 === obj2
    }

    static void setVar(String name, Object value) {
        VAR_HOLDER.get().put(name, value)
    }

    static Object getVar(String name) {
        VAR_HOLDER.get().get(name)
    }

    static Object removeVar(String name) {
        VAR_HOLDER.get().remove(name)
    }

    private static final ThreadLocal<Map<String, Object>> VAR_HOLDER = ThreadLocal.<Map<String, Object>> withInitial(() -> new LinkedHashMap<>())

    private QueryableHelper() {}
}
