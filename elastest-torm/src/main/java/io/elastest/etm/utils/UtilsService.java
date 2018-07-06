package io.elastest.etm.utils;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class UtilsService {
    final Logger logger = getLogger(lookup().lookupClass());

    @Value("${exec.mode}")
    String execMode;

    @Value("${enable.et.mini}")
    public boolean enableETMini;

    @Value("${et.etm.in.dev}")
    public boolean etmInDev;

    @Value("${test.case.start.msg.prefix}")
    public String tcStartMsgPrefix;

    @Value("${test.case.finish.msg.prefix}")
    public String tcFinishMsgPrefix;

    public boolean isElastestMini() {
        return enableETMini && execMode.equals(ElastestConstants.MODE_NORMAL);
    }

    public boolean isEtmInDevelopment() {
        return etmInDev;
    }

    public String getIso8601String() {
        return "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    }

    public String getIso8601With6MillisecondsString() {
        return "yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'";
    }

    public DateFormat getIso8601DateFormat() {
        return new SimpleDateFormat(getIso8601String());
    }

    public Date getGMT0DateFromIso8601Str(String dateStr)
            throws ParseException {
        DateFormat df = getIso8601DateFormat();
        df.setTimeZone(TimeZone.getTimeZone("GMT-0"));

        return df.parse(dateStr);
    }

    public Date getIso8601DateFromStr(String timestamp, TimeZone timezone)
            throws ParseException {
        DateFormat df = getIso8601DateFormat();
        df.setTimeZone(timezone);

        return df.parse(timestamp);
    }

    public Date getIso8601UTCDateFromStr(String timestamp)
            throws ParseException {
        return this.getIso8601DateFromStr(timestamp,
                TimeZone.getTimeZone("GMT-0"));
    }

    public String getIso8601UTCStrFromDate(Date date) throws ParseException {
        logger.debug("getIso8601UTCStrFromDate 1: {}",date);
        DateFormat df = new SimpleDateFormat(getIso8601String(), Locale.UK);
        df.setTimeZone(TimeZone.getTimeZone("GMT-0"));
        return df.format(date);
    }

    public Date getIso8601UTCDateFromDate(Date date) throws ParseException {
        String dateStr = this.getIso8601UTCStrFromDate(date);
        logger.debug("getIso8601UTCDateFromDate 2: {}",dateStr);
        return this.getIso8601UTCDateFromStr(dateStr);
    }

    /* ********************************************************************* */
    /* *** Date conversion from LogAnalyzer Date (yyyy-MM-dd'T'HH:mm:ss) *** */
    /* ********************************************************************* */

    public Date getDateUTCFromLogAnalyzerStrDate(String logAnalyzerDate)
            throws ParseException {
        try {
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss",
                    Locale.UK);

            return df.parse(logAnalyzerDate);
        } catch (Exception e) {
            DateFormat df = new SimpleDateFormat(getIso8601String(), Locale.UK);

            return df.parse(logAnalyzerDate);
        }
    }

    public String getStrIso8601With6MillisUTCFromLogAnalyzerDateStr(
            String logAnalyzerDate) throws ParseException {
        Date date = this.getDateUTCFromLogAnalyzerStrDate(logAnalyzerDate);
        DateFormat df = new SimpleDateFormat(
                getIso8601With6MillisecondsString(), Locale.UK);

        return df.format(date);
    }

    public String getStrIso8601With6MillisUTCFromLogAnalyzerDate(
            Date logAnalyzerDate) throws ParseException {
        DateFormat df = new SimpleDateFormat(
                getIso8601With6MillisecondsString(), Locale.UK);

        return df.format(logAnalyzerDate);
    }

    public String getETTestStartPrefix() {
        return tcStartMsgPrefix + " ";
    }

    public String getETTestFinishPrefix() {
        return tcFinishMsgPrefix + " ";
    }

    public Long convertToLong(Object o) {
        String stringToConvert = String.valueOf(o);
        Long convertedLong = Long.parseLong(stringToConvert);
        return convertedLong;

    }

}
