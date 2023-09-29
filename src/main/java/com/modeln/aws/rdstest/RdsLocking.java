package com.modeln.aws.rdstest;

import java.lang.management.ManagementFactory;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class RdsLocking {
  private static final String SELECT_COUNT_ID_FROM_QUEUE = "select count(id) from queue";
  private static final String INSERT_INTO_QUEUE =
      "insert into queue (locked_by) values ";
  private static final String WORK_HAS_DUPS =
      "select count(id) from work_completed group by queue_id order by 1 desc limit 0,1";
  private static final String UPDATE_QUEUE_LOCKED_BY =
      "update queue set locked_by = :lockedBy where locked_by is null and id = :id";
  private static final String INSERT_INTO_WORK =
      "insert into work_completed (queue_id,locked_by) values (:id,:lockedBy)";
  private static final String SELECT_NOT_LOCKED =
      "select id from queue where locked_by is null";
  private static String DROP_QUEUE_TABLE = "drop table if exists queue";
  private static String DROP_WORK_TABLE = "drop table if exists work_completed";
  private static String CREATE_QUEUE_TABLE = "CREATE TABLE IF NOT EXISTS queue ( id "
      + "BIGINT UNSIGNED NOT NULL AUTO_INCREMENT UNIQUE, "
      + "locked_by varchar(255), "
      + "created_date TIMESTAMP NOT NULL DEFAULT '0000-00-00 00:00:00',"
      + "updated_date TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP"
      + ",PRIMARY KEY (id))";
  private static String CREATE_WORK_TABLE = "CREATE TABLE IF NOT EXISTS work_completed "
      + "( id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT UNIQUE"
      + ",queue_id BIGINT NOT NULL,locked_by varchar(255),"
      + "created_date TIMESTAMP NOT NULL DEFAULT '0000-00-00 00:00:00')";

  @Autowired
  NamedParameterJdbcTemplate namedParameterJdbcTemplate;

  private int testDataSize = 10000;
  private int threadCount = 10;
  private int iterations = 1000;
  private ThreadPoolExecutor threadPool = null;

  public RdsLocking() {
  }

  public void createTables() {
    namedParameterJdbcTemplate.update(DROP_QUEUE_TABLE, Collections.EMPTY_MAP);
    namedParameterJdbcTemplate.update(CREATE_QUEUE_TABLE, Collections.EMPTY_MAP);
    namedParameterJdbcTemplate.update(DROP_WORK_TABLE, Collections.EMPTY_MAP);
    namedParameterJdbcTemplate.update(CREATE_WORK_TABLE, Collections.EMPTY_MAP);
  }

  public void populateTestData() {
    String sql = INSERT_INTO_QUEUE;

    Map<String, Object> params = new HashMap<>();
    params.put("lockedBy", null);
    for (int i = 0; i < testDataSize - 1; i++) {
      sql = sql + "(locked_by = :lockedBy),";
    }
    sql = sql + "(locked_by = :lockedBy)";
    namedParameterJdbcTemplate.update(sql, params);
  }

  public boolean isDataPopulated() {
    return namedParameterJdbcTemplate.queryForObject(SELECT_COUNT_ID_FROM_QUEUE,
        Collections.EMPTY_MAP, Integer.class) == testDataSize;
  }

  public void runUpdateInsert() {
    ThreadPoolExecutor threadPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(threadCount);
    for (int j = 0; j < iterations; j++) {
      List<Integer> ids = namedParameterJdbcTemplate.queryForList(
          SELECT_NOT_LOCKED, Collections.EMPTY_MAP, Integer.class);
      for (int i = 0; i < threadCount; i++) {
        threadPool.execute(new UpdateThread(ids.get(i)));
      }
      while (threadPool.getActiveCount() > 0) {
        try {
          Thread.sleep(1);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    }
  }

  public boolean isRunning() {
    if (threadPool == null) {
      return false;
    }
    return threadPool.getActiveCount() + threadPool.getTaskCount() > 0;
  }

  public Boolean duplicateDetected() {
    return namedParameterJdbcTemplate.queryForObject(
        WORK_HAS_DUPS,
        Collections.EMPTY_MAP, Integer.class) > 1;
  }

  public void setTestDataSize(int testDataSize) {
    this.testDataSize = testDataSize;
  }

  public int getThreadCount() {
    return threadCount;
  }

  public void setIterations(int iterations) {
    this.iterations = iterations;
  }

  private class UpdateThread extends Thread {
    private Integer id;

    public UpdateThread(Integer id) {
      this.id = id;
    }

    @Override
    public void run() {
      super.run();
      String lockedBy = ManagementFactory.getRuntimeMXBean().getName() +
          Thread.currentThread().getId();
      Map<String, Object> params = new HashMap<>();
      params.put("id", id);
      params.put("lockedBy", lockedBy);
      int updatedCnt = namedParameterJdbcTemplate.update(
          UPDATE_QUEUE_LOCKED_BY, params);
      if (updatedCnt > 0) {
        namedParameterJdbcTemplate.update(INSERT_INTO_WORK, params);
      }

    }
  }
}
