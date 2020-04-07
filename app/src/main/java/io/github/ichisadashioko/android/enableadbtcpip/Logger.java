package io.github.ichisadashioko.android.enableadbtcpip;

import android.text.Html;
import android.util.Log;
import android.widget.TextView;

public class Logger {
    public static final String TAG = "EnableADBTCPIP";
    public static TextView LOG_TEXT_VIEW = null;

    public static int error(String msg) {
        if (LOG_TEXT_VIEW != null) {
            String formatText = "<font color=#ff0000>" + escapeHtml(msg) + "</font>";
            LOG_TEXT_VIEW.append(Html.fromHtml(formatText));
            LOG_TEXT_VIEW.append("\n");
        }

        return Log.e(TAG, msg);
    }

    public static int debug(String msg) {
        if (LOG_TEXT_VIEW != null) {
            String formatText = "<font color=#00ff00>" + escapeHtml(msg) + "</font>";
            LOG_TEXT_VIEW.append(Html.fromHtml(formatText));
            LOG_TEXT_VIEW.append("\n");
        }

        return Log.d(TAG, msg);
    }

    public static int info(String msg) {
        if (LOG_TEXT_VIEW != null) {
            String formatText = "<font color=#ffffff>" + escapeHtml(msg) + "</font>";
            LOG_TEXT_VIEW.append(Html.fromHtml(formatText));
            LOG_TEXT_VIEW.append("\n");
        }

        return Log.i(TAG, msg);
    }

    // https://android.googlesource.com/platform/frameworks/base/+/master/core/java/android/text/Html.java

    /**
     * Returns an HTML escaped representation of the given plain text.
     */
    public static String escapeHtml(CharSequence text) {
        StringBuilder out = new StringBuilder();
        withinStyle(out, text, 0, text.length());
        return out.toString();
    }

    private static void withinStyle(StringBuilder out, CharSequence text, int start, int end) {
        for (int i = start; i < end; i++) {
            char c = text.charAt(i);
            if (c == '<') {
                out.append("&lt;");
            } else if (c == '>') {
                out.append("&gt;");
            } else if (c == '&') {
                out.append("&amp;");
            } else if (c >= 0xD800 && c <= 0xDFFF) {
                if (c < 0xDC00 && i + 1 < end) {
                    char d = text.charAt(i + 1);
                    if (d >= 0xDC00 && d <= 0xDFFF) {
                        i++;
                        int codepoint = 0x010000 | (int) c - 0xD800 << 10 | (int) d - 0xDC00;
                        out.append("&#").append(codepoint).append(";");
                    }
                }
            } else if (c > 0x7E || c < ' ') {
                out.append("&#").append((int) c).append(";");
            } else if (c == ' ') {
                while (i + 1 < end && text.charAt(i + 1) == ' ') {
                    out.append("&nbsp;");
                    i++;
                }
                out.append(' ');
            } else {
                out.append(c);
            }
        }
    }
}
