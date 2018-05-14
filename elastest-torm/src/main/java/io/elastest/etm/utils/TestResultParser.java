package io.elastest.etm.utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.maven.plugins.surefire.report.ReportTestSuite;
import org.apache.maven.plugins.surefire.report.TestSuiteXmlParser;
import org.xml.sax.SAXException;

public class TestResultParser {

    public ReportTestSuite testSuiteStringToReportTestSuite(String testSuiteStr)
            throws UnsupportedEncodingException, ParserConfigurationException,
            SAXException, IOException {
        TestSuiteXmlParser testSuiteXmlParser = new TestSuiteXmlParser(null);
        InputStream byteArrayIs = new ByteArrayInputStream(
                testSuiteStr.getBytes());
        return testSuiteXmlParser
                .parse(new InputStreamReader(byteArrayIs, "UTF-8")).get(0);
    }
}
