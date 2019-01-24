package io.elastest.etm.utils;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
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

    @Value("${et.in.prod}")
    public boolean etInProd;

    @Value("${test.case.start.msg.prefix}")
    public String tcStartMsgPrefix;

    @Value("${test.case.finish.msg.prefix}")
    public String tcFinishMsgPrefix;

    @Value("${et.etm.incontainer}")
    private boolean etmInContainer;

    @Value("${et.public.host}")
    public String etPublicHost;

    @Value("${et.public.host.type}")
    public String etPublicHostType;

    public boolean isElastestMini() {
        return enableETMini && execMode.equals(ElastestConstants.MODE_MINI);
    }

    public boolean isEtmInDevelopment() {
        return !etInProd;
    }

    public boolean isEtmInContainer() {
        return etmInContainer;
    }

    public boolean isDefaultEtPublicHost() {
        return ElastestConstants.DEFAULT_ET_PUBLIC_HOST
                .equals(etPublicHostType);
    }

    public String getEtPublicHostType() {
        return etPublicHostType;
    }

    public String getEtPublicHostValue() {
        return etPublicHost;
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
        DateFormat df = new SimpleDateFormat(getIso8601String(), Locale.UK);
        df.setTimeZone(TimeZone.getTimeZone("GMT-0"));
        return df.format(date);
    }

    public Date getIso8601UTCDateFromDate(Date date) throws ParseException {
        String dateStr = this.getIso8601UTCStrFromDate(date);
        return this.getIso8601UTCDateFromStr(dateStr);
    }

    // RFC5424 micro

    public String truncateTimestampStrTo3Millis(String timestamp)
            throws ArrayIndexOutOfBoundsException {
        // 2018-09-26T09:33:15.942765+02:00 to 2018-09-26T09:33:15.942
        // because date does not understand more than 3 milliseconds and
        // transform them to seconds

        List<String> splitedTime = Arrays.asList(timestamp.split("\\."));
        String milliseconds = splitedTime.get(1);

        return splitedTime.get(0) + "." + milliseconds.substring(0, 3);
    }

    public String getRFC5424MicroString() {
        // return "yyyy-MM-dd'T'HH:mm:ss.SSSSSSXXX";
        return "yyyy-MM-dd'T'HH:mm:ss.SSS";
    }

    public DateFormat getRFC5424MicroDateFormat() {
        return new SimpleDateFormat(getRFC5424MicroString());
    }

    public Date getDateFromRFC5424MicroString(String dateStr)
            throws ParseException, ArrayIndexOutOfBoundsException {
        dateStr = truncateTimestampStrTo3Millis(dateStr);
        DateFormat df = getRFC5424MicroDateFormat();
        // df.setTimeZone(TimeZone.getTimeZone("GMT-0"));

        return df.parse(dateStr);
    }

    public String getRFC5424MicroStrFromDate(Date date)
            throws ParseException, ArrayIndexOutOfBoundsException {
        DateFormat df = getRFC5424MicroDateFormat();
        // df.setTimeZone(TimeZone.getTimeZone("GMT-0"));

        return df.format(date);
    }

    // other

    public Date getLocaltimeDateFromLiveDate(Date date) {
        long difference = this.getLocaltimeDifferenceFromLiveDate(date);
        return getLocaltimeDateFromLiveDate(date, difference);
    }

    public Date getLocaltimeDateFromLiveDate(Date date, long difference) {
        try {
            if (difference != 0) {
                // Received date is NOT in the same timezone
                int differenceInHours = (new Double(difference / (1000 * 3600)))
                        .intValue();
                int newHour = date.getHours() + differenceInHours;
                date.setHours(newHour);
            }
        } catch (NullPointerException e) {
            date = new Date();
        }

        return date;
    }

    public Long getLocaltimeDifferenceFromLiveDate(Date date) {
        Date currentDate = new Date();
        Long difference = new Long(0);
        try {
            // 59 min = 3540000 ms
            int allowedDifference = 3540000;
            // ms
            difference = currentDate.getTime() - date.getTime();
            // If is in the same timezone
            if (Math.abs(difference) < allowedDifference) {
                difference = new Long(0);
            }
        } catch (NullPointerException e) {
            difference = new Long(0);
        }

        return difference;
    }

    /* ********************************************************************* */
    /* *** Date conversion from LogAnalyzer Date (yyyy-MM-dd'T'HH:mm:ss) *** */
    /* ********************************************************************* */

    public Date getDateUTCFromLogAnalyzerStrDate(String logAnalyzerDate)
            throws ParseException {
        try {
            DateFormat df = new SimpleDateFormat(getIso8601String(), Locale.UK);

            return df.parse(logAnalyzerDate);
        } catch (Exception e) {
            try {
                DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss",
                        Locale.UK);

                return df.parse(logAnalyzerDate);
            } catch (Exception e1) {
                DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm",
                        Locale.UK);

                return df.parse(logAnalyzerDate);
            }
        }
    }

    public String getStrIso8601With6MillisUTCFromLogAnalyzerDateStr(
            String logAnalyzerDate) throws ParseException {
        Date date = this.getDateUTCFromLogAnalyzerStrDate(logAnalyzerDate);

        try {
            DateFormat df = new SimpleDateFormat(getIso8601String(), Locale.UK);

            return df.format(date);
        } catch (Exception e) {
            DateFormat df = new SimpleDateFormat(
                    getIso8601With6MillisecondsString(), Locale.UK);

            return df.format(date);
        }
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
