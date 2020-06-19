package org.springframework.data.relational.core.dialect;

import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class VendorSupportedTypes {

    private final HashMap<Class<?>, Set<Integer>> supportedTypeCombinations;

    public VendorSupportedTypes(HashMap<Class<?>, Set<Integer>> supportedTypeCombinations) {
        this.supportedTypeCombinations = supportedTypeCombinations;
    }

    public boolean isSupported(Class<?> clazz, Integer sqlType) {

        Set<Integer> sqlTypes = supportedTypeCombinations.get(clazz);

        if (sqlTypes == null) {
            return false;
        }

        return sqlTypes.contains(sqlType);
    }

    public static VendorSupportedTypes createDefault() {
        return new VendorSupportedTypes(new HashMap<>());
    }


    // todo this is temporary
    public static VendorSupportedTypes createPostgres() {

        HashMap<Class<?>, Set<Integer>> map = new HashMap<>();

        map.put(LocalDate.class, new HashSet<>(Arrays.asList(Types.DATE)));
        map.put(LocalTime.class, new HashSet<>(Arrays.asList(Types.TIME, Types.TIME_WITH_TIMEZONE)));
        map.put(LocalDateTime.class, new HashSet<>(Arrays.asList(Types.TIMESTAMP)));
        map.put(OffsetDateTime.class, new HashSet<>(Arrays.asList(Types.TIMESTAMP, Types.TIMESTAMP_WITH_TIMEZONE)));
        return new VendorSupportedTypes(map);
    }
}
