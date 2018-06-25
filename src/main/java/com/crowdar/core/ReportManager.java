package com.crowdar.core;

import java.io.File;
import java.util.regex.Matcher;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;

import com.crowdar.bdd.GUIStoryRunnerV2;
import com.relevantcodes.extentreports.ExtentReports;
import com.relevantcodes.extentreports.ExtentTest;
import com.relevantcodes.extentreports.LogStatus;

public abstract class ReportManager {

	public static final String REPORTS_CONTAINER_FOLDER_NAME = "reports";
	private static ExtentReports extent;
	private static ExtentTest childLogger;
	private static ExtentTest parentLogger;
	
	public static void startParentTest(String testName){
		parentLogger = getExtentReportsInstance().startTest(testName);
	}

	public static void startChildTest(String testName){
		childLogger = getExtentReportsInstance().startTest(testName);
		parentLogger.appendChild(childLogger);
	}
	
	public static void writeResult(LogStatus status, String details){
		childLogger.log(status, details);
	}

	public static void logOverParent(LogStatus status, String details){
		parentLogger.log(status, details);
	}
	
	public static String addScreenCapture(String imagePath){
		return childLogger.addScreenCapture(imagePath);
	}
	
	
	
	public static void endTest(){
		getExtentReportsInstance().endTest(parentLogger);
	}
	
	public static void endReport(){
		getExtentReportsInstance().flush();
		getExtentReportsInstance().close();
	}
	
	// en lugar de este deberia usarse el startTest.
	// quedo deprecado por que habria que ponerlo como privado, pero como se usa por todos lados, no pude.
	@Deprecated
	public static ExtentReports getExtentReportsInstance(){
		
		if(extent == null){
			extent = new ExtentReports(getReportPath().concat(getReportName()).concat(".html"), true);
			extent.addSystemInfo("Host Name", "SoftwareTestingMaterial").addSystemInfo("Environment", "Automation Testing")
				.addSystemInfo("User Name", "Juan Manuel Spoleti");
			extent.loadConfig(new File(System.getProperty("user.dir").concat(File.separator).concat("extent-config.xml")));
		}
		return extent;
	}

	public static String getReportPath() {
		String runInstance = System.getProperty("runInstance");
		String userDir = System.getProperty("user.dir");
		
		return userDir.concat(File.separator).concat(REPORTS_CONTAINER_FOLDER_NAME).concat(File.separator).concat(runInstance).concat(File.separator);
	}
	
	private static String getReportName() {
		return "CrowdarReport"; 
	}
	
	/**
	 * Method that get the path of the JBehave html that have the scenarios of the story
	 * @param testName
	 * @return string of the path from the report of JBehave
	 */
	private static String getPathFromStory(String methodName){
		String baseDir = ReportManager.getReportPath()+File.separator+"jbehave"+File.separator+"view";
		
		methodName = methodName.replace("**", "");
		methodName = methodName.replace(".story", "");
		methodName = methodName.replace(File.separator, ".");
		methodName = methodName.replace("/", ".");

		File baseDirectory = new File(baseDir);
		String absolutePathFromStory = FileUtils.listFiles(baseDirectory, new WildcardFileFilter("*" + methodName + ".html"), null).iterator().next().getAbsolutePath();
		return absolutePathFromStory;
	}
	
	public static String getScenariosHtmlRelativePath(String methodName){
		String absoluteFilePath =getPathFromStory(GUIStoryRunnerV2.getStoryLogFileName()); 
		return ".." + (absoluteFilePath.split(ReportManager.REPORTS_CONTAINER_FOLDER_NAME)[1]).replaceAll(Matcher.quoteReplacement(File.separator), "/"); 
		
	}

	public static String getRelativeHtmlPath(String fileName) {
		return "../"+System.getProperty("runInstance")+"/img/"+fileName;
	}
	
}
