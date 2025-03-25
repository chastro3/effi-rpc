/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
/**
 * Copyright (c) 2004-2011 QOS.ch
 * All rights reserved.
 * <p>
 * Permission is hereby granted, free  of charge, to any person obtaining
 * a  copy  of this  software  and  associated  documentation files  (the
 * "Software"), to  deal in  the Software without  restriction, including
 * without limitation  the rights to  use, copy, modify,  merge, publish,
 * distribute,  sublicense, and/or sell  copies of  the Software,  and to
 * permit persons to whom the Software  is furnished to do so, subject to
 * the following conditions:
 * <p>
 * The  above  copyright  notice  and  this permission  notice  shall  be
 * included in all copies or substantial portions of the Software.
 * <p>
 * THE  SOFTWARE IS  PROVIDED  "AS  IS", WITHOUT  WARRANTY  OF ANY  KIND,
 * EXPRESS OR  IMPLIED, INCLUDING  BUT NOT LIMITED  TO THE  WARRANTIES OF
 * MERCHANTABILITY,    FITNESS    FOR    A   PARTICULAR    PURPOSE    AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE,  ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package io.effi.rpc.internal.logging;

/**
 * Internal logger.
 * <em>Internal-use-only</em>
 */
public interface Logger {

    /**
     * Checks if TRACE level logging is enabled.
     *
     * @return true if TRACE level is enabled, false otherwise
     */
    boolean isTraceEnabled();

    /**
     * Logs a TRACE level message with a format string and an exception.
     *
     * @param format the format string
     * @param e      the exception to log
     * @param args   the arguments to be replaced in the format string
     */
    void trace(String format, Throwable e, Object... args);

    /**
     * Logs a TRACE level message with a format string.
     *
     * @param format the format string
     * @param args   the arguments to be replaced in the format string
     */
    void trace(String format, Object... args);

    /**
     * Logs a TRACE level message with an exception.
     *
     * @param e the exception to log
     */
    void trace(Throwable e);

    /**
     * Checks if DEBUG level logging is enabled.
     *
     * @return true if DEBUG level is enabled, false otherwise
     */
    boolean isDebugEnabled();

    /**
     * Logs a DEBUG level message with a format string and an exception.
     *
     * @param format the format string
     * @param e      the exception to log
     * @param args   the arguments to be replaced in the format string
     */
    void debug(String format, Throwable e, Object... args);

    /**
     * Logs a DEBUG level message with a format string.
     *
     * @param format the format string
     * @param args   the arguments to be replaced in the format string
     */
    void debug(String format, Object... args);

    /**
     * Logs a DEBUG level message with an exception.
     *
     * @param e the exception to log
     */
    void debug(Throwable e);

    /**
     * Checks if INFO level logging is enabled.
     *
     * @return true if INFO level is enabled, false otherwise
     */
    boolean isInfoEnabled();

    /**
     * Logs an INFO level message with a format string and an exception.
     *
     * @param format the format string
     * @param e      the exception to log
     * @param args   the arguments to be replaced in the format string
     */
    void info(String format, Throwable e, Object... args);

    /**
     * Logs an INFO level message with a format string.
     *
     * @param format the format string
     * @param args   the arguments to be replaced in the format string
     */
    void info(String format, Object... args);

    /**
     * Logs an INFO level message with an exception.
     *
     * @param e the exception to log
     */
    void info(Throwable e);

    /**
     * Checks if WARN level logging is enabled.
     *
     * @return true if WARN level is enabled, false otherwise
     */
    boolean isWarnEnabled();

    /**
     * Logs a WARN level message with a format string and an exception.
     *
     * @param format the format string
     * @param e      the exception to log
     * @param args   the arguments to be replaced in the format string
     */
    void warn(String format, Throwable e, Object... args);

    /**
     * Logs a WARN level message with a format string.
     *
     * @param format the format string
     * @param args   the arguments to be replaced in the format string
     */
    void warn(String format, Object... args);

    /**
     * Logs a WARN level message with an exception.
     *
     * @param e the exception to log
     */
    void warn(Throwable e);

    /**
     * Checks if ERROR level logging is enabled.
     *
     * @return true if ERROR level is enabled, false otherwise
     */
    boolean isErrorEnabled();

    /**
     * Logs an ERROR level message with a format string and an exception.
     *
     * @param format the format string
     * @param e      the exception to log
     * @param args   the arguments to be replaced in the format string
     */
    void error(String format, Throwable e, Object... args);

    /**
     * Logs an ERROR level message with a format string.
     *
     * @param format the format string
     * @param args   the arguments to be replaced in the format string
     */
    void error(String format, Object... args);

    /**
     * Logs an ERROR level message with an exception.
     *
     * @param e the exception to log
     */
    void error(Throwable e);
}

