package com.crowdar.driver;

import com.crowdar.core.PropertyManager;
import com.crowdar.driver.setupStrategy.SetupStrategy;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.UnreachableBrowserException;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;

class DriverFactory {

    private static Logger logger = Logger.getLogger(DriverFactory.class);
    private static final String DEFAULT_STRATEGY = "NoneStrategy";
    private static final String STRATEGY_CLASS = "com.crowdar.driver.setupStrategy.%s";

    protected static RemoteWebDriver createDriver() {
        try {

            ProjectTypeEnum projectType = ProjectTypeEnum.get(PropertyManager.getProperty(ProjectTypeEnum.PROJECT_TYPE_KEY));
            String strategy = PropertyManager.getProperty("crowdar.setupStrategy");
            Class<?> StrategyClass;
            if (StringUtils.isEmpty(strategy)) {
                StrategyClass = Class.forName(String.format(STRATEGY_CLASS, DEFAULT_STRATEGY));
            } else {
                StrategyClass = Class.forName(String.format(STRATEGY_CLASS, strategy));
            }
            SetupStrategy setupStrategy = (SetupStrategy) StrategyClass.getDeclaredConstructor().newInstance();

            setupStrategy.beforeDriverStartSetup(projectType);

            RemoteWebDriver driver;

            if (StringUtils.isEmpty(PropertyManager.getProperty("crowdar.driverHub"))) {
                Constructor<?> constructor = projectType.getLocalDriverImplementation().getDeclaredConstructor(Capabilities.class);
                driver = (RemoteWebDriver) constructor.newInstance(projectType.getDesiredCapabilities());
            } else {
                Constructor<?> constructor = projectType.getRemoteDriverImplementation().getDeclaredConstructor(URL.class, Capabilities.class);
                URL url = new URL(PropertyManager.getProperty("crowdar.driverHub"));
                driver = (RemoteWebDriver) constructor.newInstance(url, projectType.getDesiredCapabilities());
            }

            setupStrategy.afterDriverStartSetup(driver);
            return driver;

        } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException
                | IllegalArgumentException | InvocationTargetException | UnreachableBrowserException e) {
            logger.error(e.getCause());
            throw new RuntimeException("Error creating driver", e.getCause());

        } catch (ClassNotFoundException e) {
            logger.error("error loading strategy class: com.crowdar.driver.setupStrategy." + PropertyManager.getProperty("crowdar.setupStrategy"));
            logger.error("Verify if path exist.");
            throw new RuntimeException("Error creating driver");

        } catch (MalformedURLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }


}
