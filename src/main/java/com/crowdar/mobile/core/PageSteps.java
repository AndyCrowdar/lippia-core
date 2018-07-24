package com.crowdar.mobile.core;

import io.appium.java_client.AppiumDriver;

/**
 * @author jCarames
 */
public abstract class PageSteps {

    private static String PAGE_NOT_DISPLAYED_MESSAGE = "'%s' page not displayed";

    private AppiumDriver driver;

    public PageSteps(AppiumDriver driver) {
        this.driver = driver;
    }

    public AppiumDriver getDriver() {
        return driver;
    }

    /**
     * Returns a standard message for a page not displayed error
     *
     * @param pageName
     */
    public String pageNotDisplayedMessage(String pageName) {
        return String.format(PAGE_NOT_DISPLAYED_MESSAGE, pageName);
    }

}