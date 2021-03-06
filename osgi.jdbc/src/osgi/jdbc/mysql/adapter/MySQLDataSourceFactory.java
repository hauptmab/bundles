/*
 * Copyright (c) OSGi Alliance (2013). All Rights Reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package osgi.jdbc.mysql.adapter;

import java.sql.Driver;
import java.sql.SQLException;
import java.util.Properties;
import javax.sql.ConnectionPoolDataSource;
import javax.sql.DataSource;
import javax.sql.XADataSource;
import org.osgi.service.jdbc.DataSourceFactory;
import aQute.bnd.annotation.component.Component;
import com.mysql.jdbc.jdbc2.optional.MysqlConnectionPoolDataSource;
import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import com.mysql.jdbc.jdbc2.optional.MysqlXADataSource;

@Component(properties = {
		"osgi.jdbc.driver.class=com.mysql.jdbc.Driver", "osgi.jdbc.driver.name=MySql",
		"osgi.jdbc.driver.version=5.1.25"
})
public class MySQLDataSourceFactory implements DataSourceFactory {

	@Override
	public DataSource createDataSource(Properties props) throws SQLException {
		return setProperties(new MysqlDataSource(), props);

	}

	@Override
	public ConnectionPoolDataSource createConnectionPoolDataSource(Properties props) throws SQLException {
		return setProperties(new MysqlConnectionPoolDataSource(), props);
	}

	@Override
	public XADataSource createXADataSource(Properties props) throws SQLException {
		return setProperties(new MysqlXADataSource(), props);
	}

	@Override
	public Driver createDriver(Properties props) throws SQLException {
		return new com.mysql.jdbc.Driver();
	}

	private <T extends MysqlDataSource> T setProperties(T ds, Properties properties) throws SQLException {
		Properties props = (Properties) properties.clone();
		String url = (String) props.remove(DataSourceFactory.JDBC_URL);
		if (url == null)
			throw new SQLException("You must use the URL for a MySQL driver");

		if (!url.startsWith("jdbc:mysql:"))
			throw new SQLException("This URL is not for a MySQL driver: " + url);

		ds.setUrl(url);

		String password = (String) props.remove(DataSourceFactory.JDBC_PASSWORD);
		String user = (String) props.remove(DataSourceFactory.JDBC_USER);

		if (password != null && user != null) {
			ds.setUser(user);
			ds.setPassword(password);
		} else if (user != null)
			throw new SQLException("If you set the user for this connection you must also set the password: " + url
					+ " for " + user);
		else if (password != null)
			throw new SQLException("Password is set for this connection but the user is not set: " + url);

		if (!props.isEmpty())
			throw new SQLException("cannot set properties " + props.keySet());

		return ds;
	}
}
