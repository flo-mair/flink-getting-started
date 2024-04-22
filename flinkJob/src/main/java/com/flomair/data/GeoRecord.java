package com.flomair.data;

import com.flomair.serdes.PointListTypeInfoFactory;
import lombok.*;
import org.apache.flink.api.common.typeinfo.TypeInfo;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString

public class GeoRecord {

    private long timestamp;
    @TypeInfo(PointListTypeInfoFactory.class)
    private List<Float> point;
}
