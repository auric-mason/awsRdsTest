package com.modeln.aws.rdstest;

import java.time.LocalDateTime;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * Test multiple updates via threads and apps to single record in rds with different values and is
 * successful *
 */
public class App {
  public static void main(String[] args) {
    AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(
        AppConfig.class);
    try {
      RdsLocking lockingTest = context.getBean(RdsLocking.class);

      if (args.length > 1) {
        Integer testSize = Integer.parseInt(args[1]);
        lockingTest.setTestDataSize(testSize);
        Integer iterations = Math.round(testSize / lockingTest.getThreadCount()) + 1;
        lockingTest.setIterations(iterations);
      }

      if (args.length > 0 && Boolean.parseBoolean(args[0])) {
        lockingTest.createTables();
        lockingTest.populateTestData();
      } else {
        while (!lockingTest.isDataPopulated()) {
          try {
            Thread.sleep(500);
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        }
      }
      LocalDateTime now = LocalDateTime.now();
      while (now.getMinute() != 0 && now.getSecond() != 0) {
        now = LocalDateTime.now();
      }
      lockingTest.runUpdateInsert();
      while (lockingTest.isRunning()) {

        try {
          Thread.sleep(1000);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
      Boolean hasDups = lockingTest.duplicateDetected();
      System.out.println("Duplicate updates detected: " + hasDups.toString());
    } catch (Throwable t) {
      System.out.println("Error in execution:" + t);
      System.exit(1);
    } finally {
      context.close();
    }
    System.exit(0);
  }

}
