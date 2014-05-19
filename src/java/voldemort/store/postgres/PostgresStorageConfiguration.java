/*
 * Copyright 2008-2009 LinkedIn, Inc
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package voldemort.store.postgres;

import java.sql.SQLException;

import org.apache.commons.dbcp.BasicDataSource;

import voldemort.VoldemortException;
import voldemort.routing.RoutingStrategy;
import voldemort.server.VoldemortConfig;
import voldemort.store.StorageConfiguration;
import voldemort.store.StorageEngine;
import voldemort.store.StoreDefinition;
import voldemort.utils.ByteArray;

public class PostgresStorageConfiguration implements StorageConfiguration {

    public static final String TYPE_NAME = "postgres";

    private BasicDataSource dataSource;

    public PostgresStorageConfiguration(VoldemortConfig config) {
        BasicDataSource ds = new BasicDataSource();
        ds.setUrl("jdbc:postgresql://" + config.getPostgresHost() + ":" + config.getPostgresPort()
                  + "/" + config.getPostgresDatabaseName());
        ds.setUsername(config.getPostgresUsername());
        ds.setPassword(config.getPostgresPassword());
        ds.setDriverClassName("org.postgresql.Driver");
        this.dataSource = ds;
    }

    public StorageEngine<ByteArray, byte[], byte[]> getStore(StoreDefinition storeDef,
                                                             RoutingStrategy strategy) {
        return new PostgresStorageEngine(storeDef.getName(), dataSource);
    }

    public String getType() {
        return TYPE_NAME;
    }

    public void close() {
        try {
            this.dataSource.close();
        } catch(SQLException e) {
            throw new VoldemortException("Exception while closing connection pool.", e);
        }
    }

    public void update(StoreDefinition storeDef) {
        throw new VoldemortException("Storage config updates not permitted for "
                                     + this.getClass().getCanonicalName());
    }

    // Nothing to do here: we're not tracking the created storage engine.
    @Override
    public void removeStorageEngine(StorageEngine<ByteArray, byte[], byte[]> engine) {}
}
