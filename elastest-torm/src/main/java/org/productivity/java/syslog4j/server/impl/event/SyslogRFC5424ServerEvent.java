package org.productivity.java.syslog4j.server.impl.event;

import java.util.List;
import java.net.InetAddress;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import org.productivity.java.syslog4j.SyslogConstants;
import org.productivity.java.syslog4j.server.SyslogServerEventIF;
import org.productivity.java.syslog4j.util.SyslogUtility;

/**
 * SyslogServerEvent provides an implementation of the SyslogServerEventIF
 * interface.
 * 
 * <p>
 * Syslog4j is licensed under the Lesser GNU Public License v2.1. A copy of the
 * LGPL license is available in the META-INF folder in all distributions of
 * Syslog4j and in the base directory of the "doc" ZIP.
 * </p>
 * 
 * @author &lt;syslog4j@productivity.org&gt;
 * @version $Id: SyslogServerEvent.java,v 1.9 2011/01/11 06:21:15 cvs Exp $
 */
public class SyslogRFC5424ServerEvent implements SyslogServerEventIF {
    private static final long serialVersionUID = 6136043067089899962L;

    public static final String DATE_FORMAT = "MMM dd HH:mm:ss yyyy";
    public static final String SYSLOG_RFC5424_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS";

    protected String charSet = SyslogConstants.CHAR_SET_DEFAULT;
    protected String rawString = null;
    protected byte[] rawBytes = null;
    protected int rawLength = -1;
    protected Date date = null;
    protected int level = -1;
    protected int facility = -1;
    protected String hostName = null;
    protected String host = null;
    protected String appName = null;
    protected Long procid = null;
    protected String msgid;
    protected boolean isHostStrippedFromMessage = false;
    protected String message = null;
    protected InetAddress inetAddress = null;
    protected ArrayList<String> currentTraceArray = new ArrayList<String>();
    protected ArrayList<String> traceInfoArray = new ArrayList<String>();

    protected SyslogRFC5424ServerEvent() {
    }

    public SyslogRFC5424ServerEvent(final String message,
            InetAddress inetAddress) {
        initialize(message, inetAddress);

        parse();
    }

    public SyslogRFC5424ServerEvent(final byte[] message, int length,
            InetAddress inetAddress) {
        initialize(message, length, inetAddress);

        parse();
    }

    public SyslogRFC5424ServerEvent(SyslogRFC5424ServerEvent previousEvent) {
        this.charSet = previousEvent.charSet;
        this.rawString = previousEvent.rawString;
        this.rawBytes = previousEvent.rawBytes;
        this.rawLength = previousEvent.rawLength;
        this.date = previousEvent.date;
        this.level = previousEvent.level;
        this.facility = previousEvent.facility;
        this.hostName = previousEvent.hostName;
        this.host = previousEvent.host;
        this.appName = previousEvent.appName;
        this.procid = previousEvent.procid;
        this.msgid = previousEvent.msgid;
        this.isHostStrippedFromMessage = previousEvent.isHostStrippedFromMessage;
        this.message = previousEvent.message;
        this.inetAddress = previousEvent.inetAddress;
        this.currentTraceArray = previousEvent.currentTraceArray;
        this.traceInfoArray = previousEvent.traceInfoArray;
    }

    public SyslogRFC5424ServerEvent(final String message,
            InetAddress inetAddress, SyslogRFC5424ServerEvent previousEvent) {
        // From previous trace (for splitted traces)
        this(previousEvent);
        initialize(message, inetAddress);
    }

    protected void initialize(final String message, InetAddress inetAddress) {
        this.rawString = message;
        this.rawLength = message.length();
        this.inetAddress = inetAddress;

        this.message = message;
    }

    protected void initialize(final byte[] message, int length,
            InetAddress inetAddress) {
        this.rawBytes = message;
        this.rawLength = length;
        this.inetAddress = inetAddress;
    }

    /* ******************************************* */
    /* ************** Parse methods ************** */
    /* ******************************************* */

    protected void parseMsgid() {
        if (this.isValidCurrentTraceArray() && this.isValidTraceInfoArray()
                && this.traceInfoArray.size() >= 6) {
            this.msgid = this.traceInfoArray.get(5);
        }
    }

    protected void parseProcid() {
        if (this.isValidCurrentTraceArray() && this.isValidTraceInfoArray()
                && this.traceInfoArray.size() >= 5) {
            try {
                this.procid = Long.valueOf(this.traceInfoArray.get(4));
            } catch (Exception e) {
            }
        }
    }

    protected void parseAppName() {
        if (this.isValidCurrentTraceArray() && this.isValidTraceInfoArray()
                && this.traceInfoArray.size() >= 4) {
            this.appName = this.traceInfoArray.get(3).trim();
        }
    }

    protected void parseHost() {
        if (this.isValidCurrentTraceArray() && this.isValidTraceInfoArray()
                && this.traceInfoArray.size() >= 3) {
            String providedHost = this.traceInfoArray.get(2).trim();
            this.hostName = providedHost;

            String hostAddress = null;
            String hostName = null;

            hostAddress = this.inetAddress.getHostAddress();

            if (providedHost.equalsIgnoreCase(hostAddress)) {
                this.host = hostAddress;
                isHostStrippedFromMessage = true;
            }

            if (this.host == null) {
                hostName = this.inetAddress.getHostName();

                if (!hostName.equalsIgnoreCase(hostAddress)) {
                    if (providedHost.equalsIgnoreCase(hostName)) {
                        this.host = hostName;
                        isHostStrippedFromMessage = true;
                    }

                    if (this.host == null) {
                        int j = hostName.indexOf('.');

                        if (j > -1) {
                            hostName = hostName.substring(0, j);
                        }

                        if (providedHost.equalsIgnoreCase(hostName)) {
                            this.host = hostName;
                            isHostStrippedFromMessage = true;
                        }
                    }
                }
            }

            if (this.host == null) {
                this.host = (hostName != null) ? hostName : hostAddress;
            }
        }
    }

    protected String truncateTimestampStrTo3Millis(String timestamp)
            throws ArrayIndexOutOfBoundsException {
        // 2018-09-26T09:33:15.942765+02:00 to 2018-09-26T09:33:15.942
        // because date does not understand more than 3 milliseconds and
        // transform them to seconds

        List<String> splitedTime = Arrays.asList(timestamp.split("\\."));
        String milliseconds = splitedTime.get(1);

        return splitedTime.get(0) + "." + milliseconds.substring(0, 3);
    }

    protected void parseDate() {

        if (this.isValidCurrentTraceArray() && this.isValidTraceInfoArray()
                && this.traceInfoArray.size() >= 2) {
            String rawDate = this.traceInfoArray.get(1);
            try {
                rawDate = this.truncateTimestampStrTo3Millis(rawDate);
                DateFormat dateFormat = new SimpleDateFormat(
                        SYSLOG_RFC5424_DATE_FORMAT);
                this.date = dateFormat.parse(rawDate);
            } catch (ArrayIndexOutOfBoundsException | ParseException pe) {
                System.out.println("Date exception: " + rawDate
                        + " . Creating new date...");
                this.date = new Date();
            }
        }
    }

    protected void parsePriority() {
        if (this.isValidCurrentTraceArray() && this.isValidTraceInfoArray()) {
            String rawPriority = this.traceInfoArray.get(0);
            if (rawPriority.charAt(0) == '<') {
                int i = rawPriority.indexOf(">");

                if (i <= 4 && i > -1) {
                    // e.g.: Get 30 in <30>
                    String priorityStr = rawPriority.substring(1, i);

                    int priority = 0;
                    try {
                        priority = Integer.parseInt(priorityStr);
                        this.facility = priority >> 3;
                        this.level = priority - (this.facility << 3);

                    } catch (NumberFormatException nfe) {
                        //
                    }

                }
            }
        }
    }

    protected void parse() {
        initCurrentTraceArray();
        if (this.isValidCurrentTraceArray() && this.isValidTraceInfoArray()) {
            this.message = this.currentTraceArray.get(1);

            parsePriority();
            parseDate();
            parseHost();
            parseAppName();
            parseProcid();
            parseMsgid();
        }
    }

    protected void initCurrentTraceArray() {
        if (this.message == null) {
            this.message = SyslogUtility.newString(this, this.rawBytes,
                    this.rawLength);
        }
        this.currentTraceArray = new ArrayList<String>(
                Arrays.asList(this.message.split(" - ", 2)));
        if (!this.isValidCurrentTraceArray()) {
            // TODO
        }

        this.traceInfoArray = new ArrayList<String>(
                Arrays.asList(this.currentTraceArray.get(0).split(" ")));

    }

    public boolean isValidCurrentTraceArray() {
        boolean valid = this.currentTraceArray != null
                && this.currentTraceArray.size() == 2;
        // If empty msg
        if (!valid && this.currentTraceArray != null
                && this.currentTraceArray.size() == 1) {
            this.currentTraceArray.add("");
            valid = true;
        }
        return valid;
    }

    protected boolean isValidTraceInfoArray() {
        return this.traceInfoArray != null && this.traceInfoArray.size() > 0;
    }

    public int getFacility() {
        return this.facility;
    }

    public void setFacility(int facility) {
        this.facility = facility;
    }

    public byte[] getRaw() {
        if (this.rawString != null) {
            byte[] rawStringBytes = SyslogUtility.getBytes(this,
                    this.rawString);

            return rawStringBytes;

        } else if (this.rawBytes.length == this.rawLength) {
            return this.rawBytes;

        } else {
            byte[] newRawBytes = new byte[this.rawLength];
            System.arraycopy(this.rawBytes, 0, newRawBytes, 0, this.rawLength);

            return newRawBytes;
        }
    }

    public String getRawString() {
        return this.rawString;
    }

    public int getRawLength() {
        return this.rawLength;
    }

    public Date getDate() {
        return this.date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public int getLevel() {
        return this.level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getHost() {
        return this.host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public boolean isHostStrippedFromMessage() {
        return isHostStrippedFromMessage;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public Long getProcid() {
        return procid;
    }

    public void setProcid(Long procid) {
        this.procid = procid;
    }

    public String getMsgid() {
        return msgid;
    }

    public void setMsgid(String msgid) {
        this.msgid = msgid;
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getCharSet() {
        return this.charSet;
    }

    public void setCharSet(String charSet) {
        this.charSet = charSet;
    }

    @Override
    public String toString() {
        return "SyslogRFC5424ServerEvent [charSet=" + charSet + ", rawString="
                + rawString + ", rawBytes=" + Arrays.toString(rawBytes)
                + ", rawLength=" + rawLength + ", date=" + date + ", level="
                + level + ", facility=" + facility + ", hostName=" + hostName
                + ", host=" + host + ", appName=" + appName + ", procid="
                + procid + ", msgid=" + msgid + ", isHostStrippedFromMessage="
                + isHostStrippedFromMessage + ", message=" + message
                + ", inetAddress=" + inetAddress + ", currentTraceArray="
                + currentTraceArray + ", traceInfoArray=" + traceInfoArray
                + "]";
    }

}
