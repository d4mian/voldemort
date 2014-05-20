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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import voldemort.store.PersistenceFailureException;
import voldemort.store.rdbms.RdbmsStorageEngine;

/**
 * A StorageEngine that uses Postgres for persistence
 * 
 * 
 */
public class PostgresStorageEngine extends RdbmsStorageEngine {

    private static final int POSTGRES_ERR_DUP_KEY = 23505;

    public PostgresStorageEngine(String name, DataSource datasource) {
        super(name, datasource);
    }

    @Override
    protected boolean tableExists() {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        String select = "select * from pg_catalog.pg_tables where tablename like  '" + getName()
                        + "'";
        try {
            conn = this.datasource.getConnection();
            stmt = conn.prepareStatement(select);
            rs = stmt.executeQuery();
            return rs.next();
        } catch(SQLException e) {
            throw new PersistenceFailureException("SQLException while checking for table existence!",
                                                  e);
        } finally {
            tryClose(rs);
            tryClose(stmt);
            tryClose(conn);
        }
    }

    @Override
    protected void create() {
        execute("create table " + getName() + " (key_ bytea not null, version_ bytea not null, "
                + " value_ bytea, primary key(key_, version_))");
    }

    @Override
    protected boolean areKeyOrValueAlreadyInUse(SQLException e) {
        return e.getErrorCode() == POSTGRES_ERR_DUP_KEY;
    }
}
