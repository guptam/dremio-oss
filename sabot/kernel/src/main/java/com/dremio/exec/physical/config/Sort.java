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
package com.dremio.exec.physical.config;

import java.util.List;

import com.dremio.common.logical.data.Order.Ordering;
import com.dremio.exec.expr.fn.FunctionLookupContext;
import com.dremio.exec.physical.base.AbstractSingle;
import com.dremio.exec.physical.base.PhysicalOperator;
import com.dremio.exec.physical.base.PhysicalVisitor;
import com.dremio.exec.proto.UserBitShared.CoreOperatorType;
import com.dremio.exec.record.BatchSchema;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("sort")
public class Sort extends AbstractSingle{
  static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(Sort.class);

  protected final List<Ordering> orderings;
  protected boolean reverse = false;

  @JsonCreator
  public Sort(@JsonProperty("child") PhysicalOperator child, @JsonProperty("orderings") List<Ordering> orderings, @JsonProperty("reverse") boolean reverse) {
    super(child);
    this.orderings = orderings;
    this.reverse = reverse;
  }

  public List<Ordering> getOrderings() {
    return orderings;
  }

  public boolean getReverse() {
    return reverse;
  }

  @Override
  public <T, X, E extends Throwable> T accept(PhysicalVisitor<T, X, E> physicalVisitor, X value) throws E{
    return physicalVisitor.visitSort(this, value);
  }

  @Override
  protected PhysicalOperator getNewWithChild(PhysicalOperator child) {
    return new Sort(child, orderings, reverse);
  }

  @Override
  public BatchSchema getSchema(FunctionLookupContext context) {
    return child.getSchema(context);
  }

  @Override
  public int getOperatorType() {
    return CoreOperatorType.OLD_SORT_VALUE;
  }
}
