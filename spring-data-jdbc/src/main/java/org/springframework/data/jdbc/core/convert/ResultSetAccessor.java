/*
 * Copyright 2020 the original author or authors.
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
package org.springframework.data.jdbc.core.convert;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mapping.MappingException;
import org.springframework.lang.Nullable;
import org.springframework.util.LinkedCaseInsensitiveMap;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Map;
import java.util.Set;

/**
 * Wrapper value object for a {@link java.sql.ResultSet} to be able to access raw values by
 * {@link org.springframework.data.relational.core.mapping.RelationalPersistentProperty} references. Provides fast
 * lookup of columns by name, including for absent columns.
 *
 * @author Jens Schauder
 * @author Mark Paluch
 * @since 2.0
 */
class ResultSetAccessor {

	private static final Logger LOG = LoggerFactory.getLogger(ResultSetAccessor.class);

	private final ResultSet resultSet;

	private final Map<String, Integer> indexLookUp;

	ResultSetAccessor(ResultSet resultSet) {

		this.resultSet = resultSet;
		this.indexLookUp = indexColumns(resultSet);
	}

	private static Map<String, Integer> indexColumns(ResultSet resultSet) {

		try {

			ResultSetMetaData metaData = resultSet.getMetaData();
			int columnCount = metaData.getColumnCount();

			Map<String, Integer> index = new LinkedCaseInsensitiveMap<>(columnCount);

			for (int i = 1; i <= columnCount; i++) {

				String label = metaData.getColumnLabel(i);

				if (index.containsKey(label)) {
					LOG.warn("ResultSet contains {} multiple times", label);
					continue;
				}

				index.put(label, i);
			}

			return index;
		} catch (SQLException se) {
			throw new MappingException("Cannot obtain result metadata", se);
		}
	}

	/**
	 * Returns the value if the result set contains the {@code columnName}.
	 *
	 * @param columnName the column name (label).
	 * @param targetType
	 * @param vendorSpecificSupportedTypes
	 * @return
	 * @see ResultSet#getObject(int)
	 */
	@Nullable
	public Object getObject(String columnName,
							Class<?> targetType,
							Set<Class<?>> vendorSpecificSupportedTypes) {

		try {

			int index = findColumnIndex(columnName);

			// this is the jdbc ResultSet.
			// Returns an sql.Timestamp by default
			// For postgresql, if called with resultSet.getObject(index, OffsetDateTime.class), it will map correctly to OffsetDateTime.

			if (index <= 0) {
				return null;
			}

			if (vendorSpecificSupportedTypes.contains(targetType)) {
				return resultSet.getObject(index, targetType);
			}

			return resultSet.getObject(index);

		} catch (SQLException o_O) {
			throw new MappingException(String.format("Could not read value %s from result set!", columnName), o_O);
		}
	}

	private int findColumnIndex(String columnName) {
		return indexLookUp.getOrDefault(columnName, -1);
	}

	/**
	 * Returns {@literal true} if the result set contains the {@code columnName}.
	 *
	 * @param columnName the column name (label).
	 * @return
	 */
	public boolean hasValue(String columnName) {
		return indexLookUp.containsKey(columnName);
	}
}
