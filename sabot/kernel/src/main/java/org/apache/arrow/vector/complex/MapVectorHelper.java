/*
 * Copyright (C) 2017 Dremio Corporation
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
package org.apache.arrow.vector.complex;


import java.util.List;

import org.apache.arrow.vector.FieldVector;
import org.apache.arrow.vector.ValueVector;
import org.apache.arrow.vector.types.SerializedFieldHelper;
import org.apache.arrow.vector.types.pojo.Field;
import org.apache.arrow.vector.util.BasicTypeHelper;

import com.dremio.common.types.Types;
import com.dremio.common.types.TypeProtos.MinorType;
import com.dremio.exec.expr.TypeHelper;
import com.dremio.exec.proto.UserBitShared.NamePart;
import com.dremio.exec.proto.UserBitShared.SerializedField;
import com.dremio.exec.record.ComplexTypeHelper;
import com.google.common.base.Preconditions;

import io.netty.buffer.ArrowBuf;

public class MapVectorHelper {
  private MapVector mapVector;

  public MapVectorHelper(MapVector vector) {
    if (vector instanceof NullableMapVector) {
      throw new IllegalArgumentException("Invalid vector: " + vector);
    }
    this.mapVector = vector;
  }

  public void load(SerializedField metadata, ArrowBuf buf) {
    final List<SerializedField> fields = metadata.getChildList();
    mapVector.valueCount = metadata.getValueCount();

    int bufOffset = 0;
    for (final SerializedField child : fields) {
      final Field fieldDef = SerializedFieldHelper.create(child);

      FieldVector vector = mapVector.getChild(fieldDef.getName());
      if (vector == null) {
//         if we arrive here, we didn't have a matching vector.
        vector = BasicTypeHelper.getNewVector(fieldDef, mapVector.allocator);
        mapVector.putChild(fieldDef.getName(), vector);
      }
      if (child.getValueCount() == 0) {
        vector.clear();
      } else {
        TypeHelper.load(vector, child, buf.slice(bufOffset, child.getBufferLength()));
      }
      bufOffset += child.getBufferLength();
    }

    Preconditions.checkState(bufOffset == buf.capacity());
  }

  public void materialize(Field field) {
    List<Field> children = field.getChildren();

    for (Field child : children) {
      FieldVector v = TypeHelper.getNewVector(child, mapVector.allocator, mapVector.callBack);
      ComplexTypeHelper.materialize(v, child);
      mapVector.putChild(child.getName(), v);
    }
  }

  public SerializedField getMetadata() {
    SerializedField.Builder b = SerializedField.newBuilder()
        .setNamePart(NamePart.newBuilder().setName(mapVector.getField().getName()))
        .setMajorType(Types.optional(MinorType.MAP))
        .setBufferLength(mapVector.getBufferSize())
        .setValueCount(mapVector.valueCount);


    for(ValueVector v : mapVector.getChildren()) {
      b.addChild(TypeHelper.getMetadata(v));
    }
    return b.build();
  }
}
