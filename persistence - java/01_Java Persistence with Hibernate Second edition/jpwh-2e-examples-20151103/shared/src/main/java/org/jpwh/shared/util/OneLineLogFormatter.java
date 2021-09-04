package org.jpwh.shared.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class OneLineLogFormatter extends Formatter {

    public String format(LogRecord record) {
        StringBuffer buf = new StringBuffer(180);
        DateFormat dateFormat = new SimpleDateFormat("kk:mm:ss,SS");

        buf.append("[").append(pad(Thread.currentThread().getName(), 32)).append("] ");
        buf.append(pad(record.getLevel().toString(), 12));
        buf.append(" - ");
        buf.append(pad(dateFormat.format(new Date(record.getMillis())), 24));
        buf.append(" - ");
        buf.append(truncate(record.getLoggerName(), 30));
        buf.append(": ");
        buf.append(formatMessage(record));

        buf.append("\n");

        Throwable throwable = record.getThrown();
        if (throwable != null) {
            StringWriter sink = new StringWriter();
            throwable.printStackTrace(new PrintWriter(sink, true));
            buf.append(sink.toString());
        }

        return buf.toString();
    }

    public String pad(String s, int size) {
        if (s.length() < size) {
            for (int i = s.length(); i < size - s.length(); i++) {
                s = s + " ";
            }
        }
        return s;
    }

    public String truncate(String name, int maxLength) {
        return name.length() > maxLength ? name.substring(name.length() - maxLength) : name;
    }

}