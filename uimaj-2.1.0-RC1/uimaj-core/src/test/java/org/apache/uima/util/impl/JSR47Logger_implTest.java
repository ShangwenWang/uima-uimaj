/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.uima.util.impl;

import java.util.HashMap;
import java.util.logging.LogManager;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.apache.uima.util.Level;

/**
 * UIMA Logging Test
 * 
 */
public class JSR47Logger_implTest extends TestCase {

  private static HashMap logLevels = new HashMap(9);
  static {
    logLevels.put("OFF", Level.OFF);
    logLevels.put("SEVERE", Level.SEVERE);
    logLevels.put("WARNING", Level.WARNING);
    logLevels.put("INFO", Level.INFO);
    logLevels.put("CONFIG", Level.CONFIG);
    logLevels.put("FINE", Level.FINE);
    logLevels.put("FINER", Level.FINER);
    logLevels.put("FINEST", Level.FINEST);
    logLevels.put("ALL", Level.ALL);
  }

  public JSR47Logger_implTest(String arg0) {
    super(arg0);
  }

  public void testLogWrapperCreation() throws Exception {
    org.apache.uima.util.Logger rootLogger = JSR47Logger_impl.getInstance();
    org.apache.uima.util.Logger classLogger = JSR47Logger_impl.getInstance(this.getClass());

    // check base configuration
    Assert.assertNotNull(rootLogger);
    Assert.assertNotNull(classLogger);
    Assert.assertTrue(rootLogger.isLoggable(Level.INFO));
    Assert.assertTrue(classLogger.isLoggable(Level.INFO));
  }

  public void testIsLoggable() throws Exception {
    // create logger
    org.apache.uima.util.Logger rootLogger = JSR47Logger_impl.getInstance();
    org.apache.uima.util.Logger classLogger = JSR47Logger_impl.getInstance(this.getClass());

    Level defaultLogLevel = (Level) logLevels.get(LogManager.getLogManager().getProperty(".level"));
    
    if(defaultLogLevel == null) {
      //no log level was specified, use default log level settings "INFO" that is also 
      //used by the Java logging framework.
      defaultLogLevel = Level.INFO;
    }

    // check message logging for root logger based on default log level
    Assert.assertEquals(defaultLogLevel.isGreaterOrEqual(Level.ALL), rootLogger
            .isLoggable(Level.ALL));
    Assert.assertEquals(defaultLogLevel.isGreaterOrEqual(Level.FINEST), rootLogger
            .isLoggable(Level.FINEST));
    Assert.assertEquals(defaultLogLevel.isGreaterOrEqual(Level.FINER), rootLogger
            .isLoggable(Level.FINER));
    Assert.assertEquals(defaultLogLevel.isGreaterOrEqual(Level.FINE), rootLogger
            .isLoggable(Level.FINE));
    Assert.assertEquals(defaultLogLevel.isGreaterOrEqual(Level.CONFIG), rootLogger
            .isLoggable(Level.CONFIG));
    Assert.assertEquals(defaultLogLevel.isGreaterOrEqual(Level.INFO), rootLogger
            .isLoggable(Level.INFO));
    Assert.assertEquals(defaultLogLevel.isGreaterOrEqual(Level.WARNING), rootLogger
            .isLoggable(Level.WARNING));
    Assert.assertEquals(defaultLogLevel.isGreaterOrEqual(Level.SEVERE), rootLogger
            .isLoggable(Level.SEVERE));
    Assert.assertEquals(defaultLogLevel.isGreaterOrEqual(Level.OFF), rootLogger
            .isLoggable(Level.OFF));

    // check message logging for class logger based on default log level
    Assert.assertEquals(defaultLogLevel.isGreaterOrEqual(Level.ALL), classLogger
            .isLoggable(Level.ALL));
    Assert.assertEquals(defaultLogLevel.isGreaterOrEqual(Level.FINEST), classLogger
            .isLoggable(Level.FINEST));
    Assert.assertEquals(defaultLogLevel.isGreaterOrEqual(Level.FINER), classLogger
            .isLoggable(Level.FINER));
    Assert.assertEquals(defaultLogLevel.isGreaterOrEqual(Level.FINE), classLogger
            .isLoggable(Level.FINE));
    Assert.assertEquals(defaultLogLevel.isGreaterOrEqual(Level.CONFIG), classLogger
            .isLoggable(Level.CONFIG));
    Assert.assertEquals(defaultLogLevel.isGreaterOrEqual(Level.INFO), classLogger
            .isLoggable(Level.INFO));
    Assert.assertEquals(defaultLogLevel.isGreaterOrEqual(Level.WARNING), classLogger
            .isLoggable(Level.WARNING));
    Assert.assertEquals(defaultLogLevel.isGreaterOrEqual(Level.SEVERE), classLogger
            .isLoggable(Level.SEVERE));
    Assert.assertEquals(defaultLogLevel.isGreaterOrEqual(Level.OFF), classLogger
            .isLoggable(Level.OFF));

    // reset class logger level to OFF
    // Logger.getLogger(this.getClass().getName()).setLevel(java.util.logging.Level.OFF);
    classLogger.setLevel(Level.OFF);
    Assert.assertFalse(classLogger.isLoggable(Level.ALL));
    Assert.assertFalse(classLogger.isLoggable(Level.FINEST));
    Assert.assertFalse(classLogger.isLoggable(Level.FINER));
    Assert.assertFalse(classLogger.isLoggable(Level.FINE));
    Assert.assertFalse(classLogger.isLoggable(Level.CONFIG));
    Assert.assertFalse(classLogger.isLoggable(Level.INFO));
    Assert.assertFalse(classLogger.isLoggable(Level.WARNING));
    Assert.assertFalse(classLogger.isLoggable(Level.SEVERE));
    Assert.assertFalse(classLogger.isLoggable(Level.OFF));

    // reset class logger level to ALL
    // Logger.getLogger(this.getClass().getName()).setLevel(java.util.logging.Level.ALL);
    classLogger.setLevel(Level.ALL);
    Assert.assertTrue(classLogger.isLoggable(Level.ALL));
    Assert.assertTrue(classLogger.isLoggable(Level.FINEST));
    Assert.assertTrue(classLogger.isLoggable(Level.FINER));
    Assert.assertTrue(classLogger.isLoggable(Level.FINE));
    Assert.assertTrue(classLogger.isLoggable(Level.CONFIG));
    Assert.assertTrue(classLogger.isLoggable(Level.INFO));
    Assert.assertTrue(classLogger.isLoggable(Level.WARNING));
    Assert.assertTrue(classLogger.isLoggable(Level.SEVERE));
    Assert.assertTrue(classLogger.isLoggable(Level.OFF));

    // reset class logger level to WARNING
    // Logger.getLogger(this.getClass().getName()).setLevel(java.util.logging.Level.WARNING);
    classLogger.setLevel(Level.WARNING);
    Assert.assertFalse(classLogger.isLoggable(Level.ALL));
    Assert.assertFalse(classLogger.isLoggable(Level.FINEST));
    Assert.assertFalse(classLogger.isLoggable(Level.FINER));
    Assert.assertFalse(classLogger.isLoggable(Level.FINE));
    Assert.assertFalse(classLogger.isLoggable(Level.CONFIG));
    Assert.assertFalse(classLogger.isLoggable(Level.INFO));
    Assert.assertTrue(classLogger.isLoggable(Level.WARNING));
    Assert.assertTrue(classLogger.isLoggable(Level.SEVERE));
    Assert.assertTrue(classLogger.isLoggable(Level.OFF));

    // reset log level to default log level
    classLogger.setLevel(defaultLogLevel);
  }

  public void testMessageLogMethods() throws Exception {
    // create Logger
    org.apache.uima.util.Logger logger = JSR47Logger_impl.getInstance();
    // reset log level to INFO
    logger.setLevel(Level.INFO);

    // File file = File.createTempFile("LoggingTest","log");
    // file.deleteOnExit();

    // change output temporary file
    // logger.setOutputStream(new PrintStream(new FileOutputStream(file)));

    // log test with method log(Level,String)
    logger.log(Level.INFO, "My first test message");
    logger.log(Level.INFO, "");
    logger.log(Level.INFO, null);

    // log test with method log(Level,String,Object)
    Object obj = null;
    logger.log(Level.INFO, "My {0} test message", "second");
    logger.log(Level.INFO, "My {0} test message", new Object());
    logger.log(Level.INFO, "My {0} test message", "");
    logger.log(Level.INFO, "My {0} test message", obj);
    logger.log(Level.INFO, "", "");
    logger.log(Level.INFO, null, "");

    // log test with method log(Level,String,Object[])
    logger.log(Level.INFO, "My {0} test message", new Object[] { "third" });
    logger.log(Level.INFO, "My {0} test message", new Object[] {});
    logger.log(Level.INFO, "", new Object[] { "" });
    logger.log(Level.INFO, "", new Object[] { null });
    logger.log(Level.INFO, "My {0} test message", new Object[] { "" });
    logger.log(Level.INFO, "My {0} test message", new Object[] { null });
    logger.log(Level.INFO, null, "");

    // log test with method log(Level,String,Throwable)
    Throwable thrown = new Throwable();
    logger.log(Level.INFO, "My fourth test message", thrown);
    logger.log(Level.INFO, "", thrown);
    logger.log(Level.INFO, null, thrown);
    thrown = null;
    logger.log(Level.INFO, "My fourth test message", thrown);

    // test deprecated log method
    logger.log("My fifth test message");
    logger.log("");
    logger.log(null);

    // test deprecated logException method
    Exception ex = new Exception("My sixth test message");
    logger.logException(ex);
    logger.logException(null);
  }

  public void testMessageKeyLogMethods() throws Exception {
    // create Logger
    org.apache.uima.util.Logger logger = JSR47Logger_impl.getInstance();
    // reset log level to INFO
    logger.setLevel(Level.INFO);

    // File file = File.createTempFile("LoggingTest","log");
    // file.deleteOnExit();

    // change output temporary file
    // logger.setOutputStream(new PrintStream(new FileOutputStream(file)));

    // test deprecated log(String, String, Object[])
    String msgKey = "UIMA_logger_test";
    String bundle = "org.apache.uima.util.impl.logger_test_messages";
    logger.log(bundle, msgKey, new Object[] { "message key test" });
    logger.log(bundle, null, new Object[] { "message key test" });
    logger.log(bundle, msgKey, new Object[] { "" });
    logger.log(bundle, msgKey, new Object[] { null });

    // test method logrb(Level, String, String, String, String)
    logger.logrb(Level.INFO, null, null, bundle, msgKey);
    logger.logrb(Level.INFO, null, null, bundle, null);
    logger.logrb(Level.INFO, null, null, null, msgKey);
    logger.logrb(Level.INFO, null, null, null, null);
    logger.logrb(Level.INFO, "testClass", "testMethod", bundle, msgKey);
    logger.logrb(Level.INFO, "testClass", "testMethod", null, null);

    // test method logrb(Level, String, String, String, String, Object)
    Object obj = null;
    logger.logrb(Level.INFO, null, null, bundle, msgKey, new Object());
    logger.logrb(Level.INFO, null, null, bundle, msgKey, "message key test");
    logger.logrb(Level.INFO, null, null, bundle, null, "message key test");
    logger.logrb(Level.INFO, null, null, null, msgKey, "");
    logger.logrb(Level.INFO, null, null, null, null, "");
    logger.logrb(Level.INFO, "testClass", "testMethod", bundle, msgKey, obj);
    logger.logrb(Level.INFO, "testClass", "testMethod", null, null, obj);

    // test method logrb(Level, String, String, String, String, Object[])
    Object[] objects = null;
    logger.logrb(Level.INFO, null, null, bundle, msgKey, new Object[] {});
    logger.logrb(Level.INFO, null, null, bundle, null, new Object[] { "message key test" });
    logger.logrb(Level.INFO, null, null, null, msgKey, new Object[] { "" });
    logger.logrb(Level.INFO, null, null, null, null, new Object[] { "" });
    logger.logrb(Level.INFO, null, null, null, null, new Object[] { null });
    logger.logrb(Level.INFO, "testClass", "testMethod", bundle, msgKey, objects);
    logger.logrb(Level.INFO, "testClass", "testMethod", null, null, objects);

    // test method logrb(Level, String, String, String, String, thrown)
    Throwable thrown = new Throwable();
    logger.logrb(Level.INFO, null, null, bundle, msgKey, thrown);
    logger.logrb(Level.INFO, null, null, bundle, null, thrown);
    logger.logrb(Level.INFO, null, null, null, msgKey, thrown);
    logger.logrb(Level.INFO, null, null, null, null, thrown);
    thrown = null;
    logger.logrb(Level.INFO, "testClass", "testMethod", bundle, msgKey, thrown);
    logger.logrb(Level.INFO, "testClass", "testMethod", null, null, thrown);

  }
}
