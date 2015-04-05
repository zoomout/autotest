package auto.utils;


import net.thucydides.core.guice.Injectors;
import net.thucydides.core.util.EnvironmentVariables;
import net.thucydides.core.util.PropertiesFileLocalPreferences;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

/**
 * Load all project properties
 */
public final class PropertiesCollector {

    private static final String ENV = System.getProperty("env");
    private static final String PROJECT_PROPERTY_FILE = "src/test/resources/environment/" + ENV + "/project.properties";
    private static final String COMMON_PROPERTY_FILE = "src/main/java/auto/common/common.properties";
    private static final String REST_CLIENT_PROPERTY_FILE = "src/main/java/auto/common/restclient.properties";
    private static final String THUCYDIDES_UNIQUE_PROPERTY_FILE = "src/test/resources/environment/" + ENV
            + "/thucydides.properties";
    private static final String THUCYDIDES_COMMON_PROPERTY_FILE = "src/main/java/auto/common/thucydides_common.properties";
    private static final String THUCYDIDES_PROPERTY_FILE = "src/test/resources/thucydides.properties";
    private static final Logger LOG = ProjectLogger.getLogger(PropertiesCollector.class.getSimpleName());
    private static EnvironmentVariables environmentVariables = Injectors.getInjector()
            .getProvider(EnvironmentVariables.class).get();
    private static PropertiesCollector instance = new PropertiesCollector();
    private Properties properties;

    private PropertiesCollector() {
        this.properties = new Properties();
        mergeThucydidesConfigsAndSave(THUCYDIDES_UNIQUE_PROPERTY_FILE, THUCYDIDES_COMMON_PROPERTY_FILE);
        loadPropertiesFromFiles();
    }

    public static PropertiesCollector getInstance() {
        return instance;
    }

    private void mergeThucydidesConfigsAndSave(final String thucydidesUniquePropertyFile, final String thucydidesCommonPropertyFile) {
        try (FileOutputStream outStream = new FileOutputStream(THUCYDIDES_PROPERTY_FILE)) {
            List<FileInputStream> mergedConfigFiles = new ArrayList<>();
            mergedConfigFiles.add(new FileInputStream(thucydidesCommonPropertyFile));
            mergedConfigFiles.add(new FileInputStream(thucydidesUniquePropertyFile));


            int c;
            for (FileInputStream configFile : mergedConfigFiles) {
                while ((c = configFile.read()) != -1) {
                    outStream.write(c);
                }
                outStream.write(System.getProperty("line.separator").getBytes());
                configFile.close();
            }
            outStream.close();
            loadThucydidesPropertiesToEnvironmentEnvironment();
        } catch (IOException ex) {
            LOG.error("error merging configuration files", ex);
        } finally {
            File outStream = new File(THUCYDIDES_PROPERTY_FILE);
            outStream.deleteOnExit();
        }
    }

    private void loadThucydidesPropertiesToEnvironmentEnvironment() {
        Properties localPreferences = new Properties();
        try (FileInputStream fileInputStream = new FileInputStream(THUCYDIDES_PROPERTY_FILE)) {
            localPreferences.load(fileInputStream);
            Enumeration propertyNames = localPreferences.propertyNames();
            while (propertyNames.hasMoreElements()) {
                String propertyName = (String) propertyNames.nextElement();
                String localPropertyValue = localPreferences.getProperty(propertyName);
                String currentPropertyValue = environmentVariables.getProperty(propertyName);
                if ((currentPropertyValue == null) && (localPropertyValue != null)) {
                    environmentVariables.setProperty(propertyName, localPropertyValue);
                    LOG.debug("System property {} was successfully added", propertyName);
                }
            }
            new PropertiesFileLocalPreferences(environmentVariables);
        } catch (IOException e) {
            LOG.error("error loading thucydides properties", e);
        }
    }

    private void loadPropertiesFromFiles() {
        List<String> propertyFiles = new ArrayList<>();
        propertyFiles.add(PROJECT_PROPERTY_FILE);
        propertyFiles.add(COMMON_PROPERTY_FILE);
        propertyFiles.add(REST_CLIENT_PROPERTY_FILE);
        for (String propertyFile : propertyFiles) {
            propertiesFromFile(propertyFile);
        }
        afterPropertiesSet();
    }

    private void propertiesFromFile(final String fileName) {
        try (FileInputStream fileInputStream = new FileInputStream(fileName)) {
            properties.load(fileInputStream);
            LOG.debug("System properties were successfully loaded, file: {}", fileName);
        } catch (IOException e) {
            LOG.error("error loading properties from file {}", fileName, e);
            throw new RuntimeException(e);
        }
    }

    private void afterPropertiesSet() {
        for (Map.Entry props : properties.entrySet()) {
            String key = String.valueOf(props.getKey());
            if (StringUtils.isBlank(System.getProperty(key))) {
                System.setProperty(key, String.valueOf(props.getValue()));
                LOG.debug("System property {} was successfully added", key);
            }
        }
    }

}

