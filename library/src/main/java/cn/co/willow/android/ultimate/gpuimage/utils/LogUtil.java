/*
 *     Copyright © 2016 Fantasymaker
 *
 *     Permission is hereby granted, free of charge, to any person obtaining a copy
 *     of this software and associated documentation files (the "Software"), to deal
 *     in the Software without restriction, including without limitation the rights
 *     to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *     copies of the Software, and to permit persons to whom the Software is
 *     furnished to do so, subject to the following conditions:
 *
 *     The above copyright notice and this permission notice shall be included in all
 *     copies or substantial portions of the Software.
 *
 *     THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *     IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *     FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *     AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *     LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *     OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *     SOFTWARE.
 */

package cn.co.willow.android.ultimate.gpuimage.utils;

import android.annotation.SuppressLint;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

/**
 * 通用日志工具类
 * 对外暴露：日志记录方法、易读化方法
 * 不对外暴露（日志处理框架）：错误日志关键数据获取逻辑、表格化显示日志信息、日志记录自动流
 * <p/>
 * Created by willow.li on 16/5/27.
 */
public class LogUtil {

    public static final int TYPE_TEXT = 0;
    public static final int TYPE_JSON = 1;
    public static final int TYPE_XML  = 2;

    private static final int    JSON_INDENT = 4;
    private static final String XML_INDENT  = "4";

    private static final String LINE_TOP_LEFT         = "┏┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅";
    private static final String LINE_CENTER_LEFT      = "┣┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅";
    private static final String LINE_BOTTOM_LEFT      = "┗┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅┅";
    private static final String LINE_VERTICAL_DASH    = "┇";
    private static final String LINE_SEPARATOR        = System.getProperty("line.separator");
    private static final String CLASSNAME             = LogUtil.class.getName();
    private static final String LOCAL_LOG_FILE_PREFIX = "LogUtil_";


    private static String sLogFileDirPath = "";
    private static String sLogFileName    = "";
    private static String sLogFilePath    = "";
    private static String sDefaultTag     = "Ultra";

    private static boolean showLog        = true;                           // 是否启用日志
    private static boolean showMethodInfo = true;                           // 是否显示方法信息
    private static boolean showThreadName = true;                           // 是否显示线程信息
    private static boolean showDateTime   = true;                           // 是否显示日志信息
    private static boolean showWithFormat = true;                           // 是否启用表格格式

    @SuppressLint("SimpleDateFormat")
    private static SimpleDateFormat logFullDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    @SuppressLint("SimpleDateFormat")
    private static SimpleDateFormat logSaveDateFormat = new SimpleDateFormat("yyyy-MM-dd");


    /*错误日志关键数据获取逻辑==========================================================================*/
    /**
     * Get class simple name by object
     *
     * @param object object
     * @return object class simple name
     */
    private static String getClassSimpleName(Object object) {
        return object == null ? "" : object.getClass().getSimpleName();
    }

    /**
     * Get stack trace offset where our code reaches
     *
     * @param stackTraceElements array of StackTraceElement
     * @return offset index; -1 if no related
     */
    private static int getStackTraceOffset(StackTraceElement[] stackTraceElements) {
        boolean findLogUtil = true;
        for (int i = 0; i < stackTraceElements.length; i++) {
            StackTraceElement e    = stackTraceElements[i];
            String            name = e.getClassName();
            if (findLogUtil) {
                if (name.equals(CLASSNAME)) {
                    //reach LogUtil stack
                    findLogUtil = false;
                }
            } else {
                if (!name.equals(CLASSNAME)) {
                    //reach real code
                    return i;
                }
            }
        }
        return -1;
    }

    /**
     * Get array of StackTraceElements
     *
     * @return array of StackTraceElements
     */
    private static StackTraceElement getStackTraceElement() {
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        int                 stackTraceOffset   = getStackTraceOffset(stackTraceElements);
        return stackTraceElements[stackTraceOffset];
    }

    /**
     * Get line number of stackTraceElement
     *
     * @param stackTraceElement stackTraceElement
     * @return line number of stackTraceElement, e.g. "123"
     */
    private static int getLineNumber(StackTraceElement stackTraceElement) {
        return stackTraceElement.getLineNumber();
    }

    /**
     * Get full class name of stackTraceElement
     *
     * @param stackTraceElement stackTraceElement
     * @return full class name of stackTraceElement, e.g. "com.packagename.classame"
     */
    private static String getClassName(StackTraceElement stackTraceElement) {
        return stackTraceElement.getClassName();
    }

    private static String getSingleClassName(StackTraceElement stackTraceElement) {
        return getFileName(stackTraceElement).replace(".java", "");
    }

    /**
     * Get method name of stackTraceElement
     *
     * @param stackTraceElement stackTraceElement
     * @return method name without quote of stackTraceElement, e.g. "method"
     */
    private static String getMethodName(StackTraceElement stackTraceElement) {
        return stackTraceElement.getMethodName();
    }

    /**
     * Get file name of stackTraceElement
     *
     * @param stackTraceElement stackTraceElement
     * @return file name of stackTraceElement, e.g. "xxx.java"
     */
    private static String getFileName(StackTraceElement stackTraceElement) {
        return stackTraceElement.getFileName();
    }

    private static String getDateTime() {
        return logFullDateFormat.format(new Date());
    }


    /*表格化显示日志信息===============================================================================*/
    /**
     * Generate log message with grid
     *
     * @param msg msg
     * @return log message with grid
     */
    private static String generateGridMsg(String msg) {
        if (showWithFormat) {
            return LINE_TOP_LEFT + LINE_SEPARATOR
                    + LINE_VERTICAL_DASH + getClickableLineNumber() + LINE_SEPARATOR
                    + LINE_CENTER_LEFT + LINE_SEPARATOR
                    + addLineAhead(msg)
                    + LINE_BOTTOM_LEFT;
        } else {
            return getClickableLineNumber() + LINE_SEPARATOR + msg;
        }
    }

    /**
     * Combine infos to generate a log-clickable string
     *
     * @return log-clickable string, e.g. "com.packagename.class.method(filename.java)"
     */
    private static String getClickableLineNumber() {
        StackTraceElement stackTraceElement = getStackTraceElement();
        String            fileName          = getFileName(stackTraceElement);
        String            className         = getSingleClassName(stackTraceElement);
        int               lineNumber        = getLineNumber(stackTraceElement);
        String            methodName        = getMethodName(stackTraceElement);
        String            threadName        = Thread.currentThread().getName();
        String            dateTime          = getDateTime();
        String            result            = "";
        if (showMethodInfo) {
            result += "Method: " + className + "." + methodName + " " + "(" + fileName + ":" + lineNumber + ") " + LINE_VERTICAL_DASH + " ";
        }
        if (showThreadName) {
            result += "Thread: " + threadName + " " + LINE_VERTICAL_DASH + " ";
        }
        if (showDateTime) {
            result += "Time: " + dateTime + " " + LINE_VERTICAL_DASH + " ";
        }
        return result;
    }

    /**
     * Add a vertical line ahead of each line of string
     *
     * @param string string
     * @return string with vertical line ahead of each line
     */
    private static String addLineAhead(String string) {
        String   result       = "";
        String[] splitStrings = string.split(LINE_SEPARATOR);
        for (String splitString : splitStrings) {
            result += LINE_VERTICAL_DASH + splitString + LINE_SEPARATOR;
        }
        return result;
    }


    /*日志记录自动流==================================================================================*/
    /**
     * Save string to local dir
     *
     * @param logFileDirPath local log file dir path
     * @param msg            msg
     */
    private static void saveLogLocally(String logFileDirPath, String msg) {
        String logFileName = LOCAL_LOG_FILE_PREFIX + logSaveDateFormat.format(new Date());
        File   logFile     = createLocalFile(logFileDirPath, logFileName);
        append(logFile, generateGridMsg(msg) + LINE_SEPARATOR);
    }

    /**
     * Create a local file
     *
     * @param dirPath  dir where file in
     * @param fileName file name
     * @return file
     */
    private static File createLocalFile(String dirPath, String fileName) {
        File dir = new File(dirPath);
        if (!dir.exists()) {
            if (!dir.mkdir()) {
                //fail to mkdir
                e("Fail to create local directory: " + dirPath);
                return null;
            }
        }
        String filePath = dirPath + "/" + fileName;
        File   file     = new File(filePath);
        try {
            if (!file.exists()) {
                if (!file.createNewFile()) {
                    //fail to create file
                    e("Fail to create local file: " + filePath);
                    return null;
                }
            } else {
                //file already exists
                return file;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return file;
    }

    /**
     * Append string to logFile
     *
     * @param logFile log file
     * @param text    string to be appended
     */
    private static void append(File logFile, String text) {
        BufferedWriter bufferedWriter = null;
        try {
            bufferedWriter = new BufferedWriter(new FileWriter(logFile, true));
            bufferedWriter.append(text);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bufferedWriter != null) {
                try {
                    bufferedWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    /*日志记录方法====================================================================================*/
    /**
     * Set log tag
     *
     * @param tag log tag
     */
    public static void setTag(String tag) {
        sDefaultTag = tag;
    }

    /**
     * print a VERBOSE log
     *
     * @param tag tag
     * @param msg Message to be shown
     */
    public static void v(String tag, String msg) {
        if (showLog && (msg != null)) {
            Log.v(tag, generateGridMsg(msg));
        }
    }

    /**
     * print a VERBOSE log
     *
     * @param obj Object that use its class name to be as a tag
     * @param msg Message to be shown
     */
    public static void v(Object obj, String msg) {
        if (showLog && (msg != null)) {
            v(getClassSimpleName(obj), msg);
        }
    }

    /**
     * print a VERBOSE log using default tag
     *
     * @param msg msg
     */
    public static void v(String msg) {
        if (showLog && (msg != null)) {
            v(sDefaultTag, msg);
        }
    }

    /**
     * print a VERBOSE log with different type of format
     *
     * @param type TYPE_TEXT, TYPE_JSON, TYPE_XML
     * @param msg  msg
     */
    public static void v(int type, String msg) {
        switch (type) {
            case TYPE_JSON:
                v(formatJson(msg));
                break;
            case TYPE_XML:
                v(formatXml(msg));
                break;
            case TYPE_TEXT:
            default:
                v(msg);
                break;
        }
    }

    /**
     * print an VERBOSE log, with option to save log locally
     *
     * @param saveLocally true if save locally; Otherwise false
     * @param msg         msg
     */
    public static void v(boolean saveLocally, String msg) {
        if (showLog && (msg != null)) {
            v(sDefaultTag, msg);
        }
        if (saveLocally) {
            saveLogLocally(sLogFileDirPath, msg);
        }
    }

    /**
     * print a DEBUG log
     *
     * @param tag tag
     * @param msg Message to be shown
     */
    public static void d(String tag, String msg) {
        if (showLog && (msg != null)) {
            Log.d(tag, generateGridMsg(msg));
        }
    }

    /**
     * print a DEBUG log
     *
     * @param obj Object that use its class name to be as a tag
     * @param msg Message to be shown
     */
    public static void d(Object obj, String msg) {
        if (showLog && (msg != null)) {
            d(obj.getClass().getSimpleName(), msg);
        }
    }

    /**
     * print a DEBUG log using default tag
     *
     * @param msg msg
     */
    public static void d(String msg) {
        if (showLog && (msg != null)) {
            d(sDefaultTag, msg);
        }
    }

    /**
     * print a DEBUG log with different type of format
     *
     * @param type TYPE_TEXT, TYPE_JSON, TYPE_XML
     * @param msg  msg
     */
    public static void d(int type, String msg) {
        switch (type) {
            case TYPE_JSON:
                d(formatJson(msg));
                break;
            case TYPE_XML:
                d(formatXml(msg));
                break;
            case TYPE_TEXT:
            default:
                d(msg);
                break;
        }
    }

    /**
     * print an DEBUG log, with option to save log locally
     *
     * @param saveLocally true if save locally; Otherwise false
     * @param msg         msg
     */
    public static void d(boolean saveLocally, String msg) {
        if (showLog && (msg != null)) {
            d(sDefaultTag, msg);
        }
        if (saveLocally) {
            saveLogLocally(sLogFileDirPath, msg);
        }
    }

    /**
     * print an INFO log
     *
     * @param tag tag
     * @param msg Message to be shown
     */
    public static void i(String tag, String msg) {
        if (showLog && (msg != null)) {
            Log.i(tag, generateGridMsg(msg));
        }
    }

    /**
     * print an INFO log
     *
     * @param obj Object that use its class name to be as a tag
     * @param msg Message to be shown
     */
    public static void i(Object obj, String msg) {
        if (showLog && (msg != null)) {
            i(obj.getClass().getSimpleName(), msg);
        }
    }

    /**
     * print an INFO log using default tag
     *
     * @param msg msg
     */
    public static void i(String msg) {
        if (showLog && (msg != null)) {
            i(sDefaultTag, msg);
        }
    }

    /**
     * print an INFO log with different type of format
     *
     * @param type TYPE_TEXT, TYPE_JSON, TYPE_XML
     * @param msg  msg
     */
    public static void i(int type, String msg) {
        switch (type) {
            case TYPE_JSON:
                i(formatJson(msg));
                break;
            case TYPE_XML:
                i(formatXml(msg));
                break;
            case TYPE_TEXT:
            default:
                i(msg);
                break;
        }
    }

    /**
     * print an INFO log, with option to save log locally
     *
     * @param saveLocally true if save locally; Otherwise false
     * @param msg         msg
     */
    public static void i(boolean saveLocally, String msg) {
        if (showLog && (msg != null)) {
            i(sDefaultTag, msg);
        }
        if (saveLocally) {
            saveLogLocally(sLogFileDirPath, msg);
        }
    }

    /**
     * print a WARN log
     *
     * @param tag tag
     * @param msg Message to be shown
     */
    public static void w(String tag, String msg) {
        if (showLog && (msg != null)) {
            Log.w(tag, generateGridMsg(msg));
        }
    }

    /**
     * print a WARN log
     *
     * @param obj Object that use its class name to be as a tag
     * @param msg Message to be shown
     */
    public static void w(Object obj, String msg) {
        if (showLog && (msg != null)) {
            w(obj.getClass().getSimpleName(), msg);
        }
    }

    /**
     * print a WARN log using default tag
     *
     * @param msg msg
     */
    public static void w(String msg) {
        if (showLog && (msg != null)) {
            w(sDefaultTag, msg);
        }
    }

    /**
     * print a WARN log with different type of format
     *
     * @param type TYPE_TEXT, TYPE_JSON, TYPE_XML
     * @param msg  msg
     */
    public static void w(int type, String msg) {
        switch (type) {
            case TYPE_JSON:
                w(formatJson(msg));
                break;
            case TYPE_XML:
                w(formatXml(msg));
                break;
            case TYPE_TEXT:
            default:
                w(msg);
                break;
        }
    }

    /**
     * print an WARN log, with option to save log locally
     *
     * @param saveLocally true if save locally; Otherwise false
     * @param msg         msg
     */
    public static void w(boolean saveLocally, String msg) {
        if (showLog && (msg != null)) {
            w(sDefaultTag, msg);
        }
        if (saveLocally) {
            saveLogLocally(sLogFileDirPath, msg);
        }
    }

    /**
     * print an ERROR log
     *
     * @param tag tag
     * @param msg Message to be shown
     */
    public static void e(String tag, String msg) {
        if (showLog && (msg != null)) {
            Log.e(tag, generateGridMsg(msg));
        }
    }

    /**
     * print an ERROR log
     *
     * @param obj Object that use its class name to be as a tag
     * @param msg Message to be shown
     */
    public static void e(Object obj, String msg) {
        if (showLog && (msg != null)) {
            e(obj.getClass().getSimpleName(), msg);
        }
    }

    /**
     * print a ERROR log using default tag
     *
     * @param msg msg
     */
    public static void e(String msg) {
        if (showLog && (msg != null)) {
            e(sDefaultTag, msg);
        }
    }

    /**
     * print a ERROR log with different type of format
     *
     * @param type TYPE_TEXT, TYPE_JSON, TYPE_XML
     * @param msg  msg
     */
    public static void e(int type, String msg) {
        switch (type) {
            case TYPE_JSON:
                e(formatJson(msg));
                break;
            case TYPE_XML:
                e(formatXml(msg));
                break;
            case TYPE_TEXT:
            default:
                e(msg);
                break;
        }
    }

    /**
     * print an ERROR log, with option to save log locally
     *
     * @param saveLocally true if save locally; Otherwise false
     * @param msg         msg
     */
    public static void e(boolean saveLocally, String msg) {
        if (showLog && (msg != null)) {
            e(sDefaultTag, msg);
        }
        if (saveLocally) {
            saveLogLocally(sLogFileDirPath, msg);
        }
    }


    /*易读化方法======================================================================================*/
    /**
     * Format Json to a human readable string
     *
     * @param json json string
     * @return human readable string
     */
    public static String formatJson(String json) {
        if (TextUtils.isEmpty(json)) {
            return "Empty/Null json content";
        }
        try {
            json = json.trim();
            if (json.startsWith("{")) {
                JSONObject jsonObject = new JSONObject(json);
                return jsonObject.toString(JSON_INDENT);
            } else if (json.startsWith("[")) {
                JSONArray jsonArray = new JSONArray(json);
                return jsonArray.toString(JSON_INDENT);
            } else {
                return "Invalid Json: " + json;
            }
        } catch (JSONException e) {
            return "Invalid Json: " + json;
        }
    }

    /**
     * Format xml to a human readable string
     *
     * @param xml xml string
     * @return human readable string
     */
    public static String formatXml(String xml) {
        if (TextUtils.isEmpty(xml)) {
            return "Empty/Null xml content";
        }
        try {
            Source       xmlInput    = new StreamSource(new StringReader(xml));
            StreamResult xmlOutput   = new StreamResult(new StringWriter());
            Transformer  transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", XML_INDENT);
            transformer.transform(xmlInput, xmlOutput);
            return xmlOutput.getWriter().toString().replaceFirst(">", ">\n");
        } catch (TransformerException e) {
            return "Invalid xml";
        }
    }
}