/*
 * Copyright 2020-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.data.relational.core.dialect;

import org.springframework.data.util.Pair;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Holds sql to Java type mappings that a jdbc driver may support outside of the ones that
 * are provided by default in the {@link java.sql.ResultSet#getObject(int)} implementation.
 *
 * @author Rick Heuijerjans
 */
public class VendorSupportedTypes {

    private final Set<Pair<Class<?>, Integer>> supportedTypeCombinations;

    public VendorSupportedTypes(List<Pair<Class<?>, Integer>> pairs) {
        this.supportedTypeCombinations = new HashSet<>(pairs);
    }

    public boolean isSupported(Class<?> clazz,
                               Integer sqlType) {
        return supportedTypeCombinations.contains(Pair.of(clazz, sqlType));
    }

    public static VendorSupportedTypes createDefault() {
        return new VendorSupportedTypes(Collections.emptyList());
    }
}
