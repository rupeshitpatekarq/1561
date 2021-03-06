/*
 * Copyright 2015 Google Inc. All Rights Reserved.
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
package com.google.cloud.bigtable.hbase2_x.adapters.admin;

import java.util.HashMap;
import java.util.Map;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.client.ColumnFamilyDescriptor;
import org.apache.hadoop.hbase.client.TableDescriptor;
import org.apache.hadoop.hbase.util.Bytes;
import com.google.bigtable.admin.v2.ColumnFamily;
import com.google.bigtable.admin.v2.Table;
import com.google.cloud.bigtable.config.BigtableOptions;
import com.google.cloud.bigtable.hbase.adapters.admin.ColumnDescriptorAdapter;
import com.google.cloud.bigtable.hbase.adapters.admin.TableAdapter;

/**
 * Need this extended class as {@link TableAdapter#adapt(org.apache.hadoop.hbase.HTableDescriptor)}
 * is not binary compatible with {@link TableAdapter2x#adapt(TableDescriptor)}
 * 
 * Similarly, {@link ColumnDescriptorAdapter#adapt(HColumnDescriptor)} is not binary compatible with
 * {@link ColumnFamilyDescriptor}.
 * 
 * @author spollapally
 */
public class TableAdapter2x extends TableAdapter {
  public TableAdapter2x(BigtableOptions options, ColumnDescriptorAdapter columnDescriptorAdapter) {
    super(options, columnDescriptorAdapter);
  }

  public Table adapt(TableDescriptor desc) {
    Map<String, ColumnFamily> columnFamilies = new HashMap<>();
    for (ColumnFamilyDescriptor column : desc.getColumnFamilies()) {
      String columnName = column.getNameAsString();

      //TODO: verify if this copy is sufficient
      HColumnDescriptor hColumnDescriptor = new HColumnDescriptor(columnName);
      // TODO - copy the config and value Maps
      for (Map.Entry<String, String> entry : column.getConfiguration().entrySet()) {
        hColumnDescriptor.setConfiguration(entry.getKey(), entry.getValue());
      }
      for (Map.Entry<Bytes, Bytes> entry : column.getValues().entrySet()) {
        hColumnDescriptor.setValue(entry.getKey().get(), entry.getValue().get());
      }

      ColumnFamily columnFamily = columnDescriptorAdapter.adapt(hColumnDescriptor).build();
      columnFamilies.put(columnName, columnFamily);
    }
    return Table.newBuilder().putAllColumnFamilies(columnFamilies).build();
  }
}
