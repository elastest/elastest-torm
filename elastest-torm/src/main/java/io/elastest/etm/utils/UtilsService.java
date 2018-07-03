package io.elastest.etm.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class UtilsService {
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

    public Date getIso8061TimestampDate(String timestamp, TimeZone timezone)
            throws ParseException {
        DateFormat df = getIso8061DateFormat();
        df.setTimeZone(timezone);

        return df.parse(timestamp);
    }

    public Date getIso8061GMTTimestampDate(String timestamp)
            throws ParseException {
        return this.getIso8061TimestampDate(timestamp,
                TimeZone.getTimeZone("GMT"));
    }

    public String getIso8061TimestampStr(String timestamp, TimeZone timezone)
            throws ParseException {
        DateFormat df = getIso8061DateFormat();
        df.setTimeZone(timezone);

        return df.format(timestamp);
    }

    public String getIso8061GMTTimestampStr(String timestamp)
            throws ParseException {
        return this.getIso8061TimestampStr(timestamp,
                TimeZone.getTimeZone("GMT"));
    }

    public String getIso8061TimestampStr(Date timestamp, TimeZone timezone)
            throws ParseException {
        DateFormat df = getIso8061DateFormat();
        df.setTimeZone(timezone);

        return df.format(timestamp);
    }

    public String getIso8061GMTTimestampStr(Date timestamp)
            throws ParseException {
        return this.getIso8061TimestampStr(timestamp,
                TimeZone.getTimeZone("GMT"));
    }

    public DateFormat getIso8061DateFormat() {
        return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
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
