package com.flomair.serdes;

import org.apache.flink.api.common.typeinfo.TypeInfoFactory;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.typeutils.ListTypeInfo;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

public class PointListTypeInfoFactory extends TypeInfoFactory<List<Float>> {
    @Override
    public TypeInformation<List<Float>> createTypeInfo(Type type, Map<String, TypeInformation<?>> map) {
        return new ListTypeInfo<>(Float.class);
    }
}
