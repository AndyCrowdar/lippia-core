//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.crowdar.core;

import com.crowdar.core.pageObjects.LocatorTypesEnum;
import org.apache.log4j.Logger;
import org.openqa.selenium.By;


public class LocatorManager {

    public static By getLocator(String locatorElement, String ... locatorReplacementArgs) {
        try {
            String[] locatorProperty = new String[2];
            locatorProperty[0] = locatorElement.substring( 0, locatorElement.indexOf(":"));
            locatorProperty[1] = locatorElement.substring(locatorElement.indexOf(":")+1);

            return getLocatorInEnum(locatorProperty, locatorReplacementArgs);
        } catch (NullPointerException e){
            Logger.getLogger(LocatorManager.class).error(e.getMessage());
            throw new RuntimeException(String.format("Locator property %s was not found", locatorElement));
        }
    }

    private static By getLocatorInEnum(String[] locatorProperty, String ... locatorReplacementArgs) {
        try {
            String type = locatorProperty[0].toUpperCase();
            
            String value = String.format(locatorProperty[1], locatorReplacementArgs);
            return LocatorTypesEnum.get(type).getLocator(value);
        } catch (IndexOutOfBoundsException e) {
            Logger.getLogger(LocatorManager.class).error(e.getMessage());
            throw new RuntimeException("Locator property format is invalid. Example: css:#loginButton");
        }
    }
}
