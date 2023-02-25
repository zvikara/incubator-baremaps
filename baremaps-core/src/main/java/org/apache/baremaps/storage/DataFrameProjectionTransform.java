/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.apache.baremaps.storage;



import java.util.Iterator;
import org.apache.baremaps.collection.AbstractDataCollection;
import org.apache.baremaps.dataframe.Column;
import org.apache.baremaps.dataframe.DataFrame;
import org.apache.baremaps.dataframe.DataType;
import org.apache.baremaps.dataframe.Row;
import org.apache.baremaps.openstreetmap.utils.ProjectionTransformer;
import org.locationtech.jts.geom.Geometry;

public class DataFrameProjectionTransform extends AbstractDataCollection<Row> implements DataFrame {

  private final DataFrame dataFrame;

  private final ProjectionTransformer projectionTransformer;

  public DataFrameProjectionTransform(DataFrame dataFrame,
      ProjectionTransformer projectionTransformer) {
    this.dataFrame = dataFrame;
    this.projectionTransformer = projectionTransformer;
  }

  @Override
  public DataType dataType() {
    return dataFrame.dataType();
  }

  public Row transformProjection(Row row) {
    var columns = dataType().columns().stream()
        .filter(column -> column.type().isInstance(Geometry.class)).toList();
    for (Column column : columns) {
      var name = column.name();
      var geometry = (Geometry) row.get(name);
      row.set(name, projectionTransformer.transform(geometry));
    }
    return row;
  }

  @Override
  public Iterator<Row> iterator() {
    return dataFrame.stream().map(this::transformProjection).iterator();
  }

  @Override
  public long sizeAsLong() {
    return 0;
  }
}
