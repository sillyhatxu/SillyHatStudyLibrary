package com.sillyhat.project.basic;

import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.core.io.Resource;
import org.springframework.util.DefaultPropertiesPersister;
import org.springframework.util.PropertiesPersister;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by Administrator on 2016/12/8.
 */
public class SillyHatPropertyConfigurer extends PropertyPlaceholderConfigurer {

    private Resource[] locations;

    public void loadProperties(Properties props) throws IOException {
        if (this.locations != null) {
            PropertiesPersister propertiesPersister = new DefaultPropertiesPersister();
            for (int i = 0; i < this.locations.length; i++) {
                Resource location = this.locations[i];
                InputStream is = null;
                try {
                    is = location.getInputStream();
                    propertiesPersister.load(props, is);
                    String driverUrl = props.getProperty("database-0.connection.url");
                    String user = props.getProperty("database-0.connection.username");
                    String password = props.getProperty("database-0.connection.password");
                    if (user != null && password != null) {
                        props.setProperty("database-0.connection.username", new String(Base64.decodeBase64(user)));
                        props.setProperty("database-0.connection.password", new String(Base64.decodeBase64(password)));
                        user = props.getProperty("database-0.connection.username");
                        password = props.getProperty("database-0.connection.password");
                        driverUrl = driverUrl.replace("@", user + "/" + password + "@");
                        props.setProperty("database-0.connection.url", driverUrl);
                    }
                } finally {
                    if (is != null)
                        is.close();
                }
            }
        }
    }

    public void setLocations(Resource[] locations) {
        this.locations = locations;
    }

}

