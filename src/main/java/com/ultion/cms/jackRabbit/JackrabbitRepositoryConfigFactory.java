package com.ultion.cms.jackRabbit;

import org.apache.jackrabbit.core.config.ConfigurationException;
import org.apache.jackrabbit.core.config.RepositoryConfig;
import org.apache.jackrabbit.core.config.RepositoryConfigurationParser;
import org.springframework.beans.factory.BeanCreationException;
import org.xml.sax.InputSource;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class JackrabbitRepositoryConfigFactory {
    private static final String JCR_REP_HOME = "jcr.rep.home";

    private String jcrHome;

    private String configFilename = "repository.xml";

    private String propertiesFilename = "jackrabbit.properties";

    /**
     * Creates a JackRabbit RepositoryConfig. Reads properties from file and add default
     *
     * @return RepositoryConfig
     * @throws IOException
     * @throws ConfigurationException
     */
    public RepositoryConfig create() throws Exception {
        Properties properties = new Properties();
        try{
            // properties file has higher...
            InputStream is = JackrabbitRepositoryConfigFactory.class.getClassLoader().getResourceAsStream(propertiesFilename);
            if (is != null) {
                try {
                    Properties p = new Properties();
                    p.load(is);
                    properties.putAll(p);
                } finally {
                    is.close();
                }
            }

            // Copy jcr.rep.home to rep.home because RepositoryConfig expects it
            String home = properties.getProperty(JCR_REP_HOME);
            properties.setProperty(RepositoryConfigurationParser.REPOSITORY_HOME_VARIABLE, home);

            is = JackrabbitRepositoryConfigFactory.class.getClassLoader().getResourceAsStream(configFilename);
            if (is == null) {
                throw new FileNotFoundException(configFilename);
            }

            return RepositoryConfig.create(new InputSource(is), properties);
        } catch(ConfigurationException e){
            throw new BeanCreationException("Unable to configure repository with: " + configFilename + " and " + properties);
        }
    }

    /**
     * Get the working-dir-relative (or absolute) filename of Jackrabbit home.
     *
     * @return
     */
    public String getJcrHome() {
        return jcrHome;
    }

    /**
     * Set the working-dir-relative (or absolute) filename of Jackrabbit home.
     * If not set, the value of system property jcr.rep.home will be used.
     *
     * @param jcrHome
     */
    public void setJcrHome(String jcrHome) {
        this.jcrHome = jcrHome;
    }

    public String getConfigFilename() {
        return configFilename;
    }

    /**
     * Set the classpath-relative filename of config file (repository.xml)
     *
     * @param configFilename
     */
    public void setConfigFilename(String configFilename) {
        this.configFilename = configFilename;
    }

    public String getPropertiesFilename() {
        return propertiesFilename;
    }

    /**
     * Set the classpath-relative filename of properties-file.
     *
     * @param propertiesFilename
     */
    public void setPropertiesFilename(String propertiesFilename) {
        this.propertiesFilename = propertiesFilename;
    }

}
