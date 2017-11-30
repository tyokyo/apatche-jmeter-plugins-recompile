package kg.apc.jmeter.threads;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import kg.apc.jmeter.JMeterPluginsUtils;
import org.apache.jmeter.gui.util.PowerTableModel;
import org.apache.jmeter.testelement.TestStateListener;
import org.apache.jmeter.testelement.property.CollectionProperty;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.testelement.property.NullProperty;
import org.apache.jmeter.testelement.property.PropertyIterator;
import org.apache.jmeter.threads.JMeterThread;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

public class UltimateThreadGroup extends AbstractSimpleThreadGroup
  implements Serializable, TestStateListener
{
  private static final Logger log = LoggingManager.getLoggerForClass();
  public static final String DATA_PROPERTY = "ultimatethreadgroupdata";
  public static final String EXTERNAL_DATA_PROPERTY = "threads_schedule";
  public static final int START_THREADS_CNT_FIELD_NO = 0;
  public static final int INIT_DELAY_FIELD_NO = 1;
  public static final int STARTUP_TIME_FIELD_NO = 2;
  public static final int HOLD_LOAD_FOR_FIELD_NO = 3;
  public static final int SHUTDOWN_TIME_FIELD_NO = 4;
  private PropertyIterator scheduleIT;
  private int threadsToSchedule;
  private CollectionProperty currentRecord;

  protected void scheduleThread(JMeterThread thread, long tgStartTime)
  {
    log.debug("Scheduling thread: " + thread.getThreadName());
    if (this.threadsToSchedule < 1) {
      if (!this.scheduleIT.hasNext()) {
        throw new RuntimeException("Not enough schedule records for thread #" + thread.getThreadName());
      }

      this.currentRecord = ((CollectionProperty)this.scheduleIT.next());
      this.threadsToSchedule = this.currentRecord.get(0).getIntValue();
    }

    int numThreads = this.currentRecord.get(0).getIntValue();
    int initialDelay = this.currentRecord.get(1).getIntValue();
    int startRampUp = this.currentRecord.get(2).getIntValue();
    int flightTime = this.currentRecord.get(3).getIntValue();
    int endRampUp = this.currentRecord.get(4).getIntValue();

    long ascentPoint = tgStartTime + 1000 * initialDelay;
    int rampUpDelayForThread = (int)Math.floor(1000 * startRampUp * this.threadsToSchedule / numThreads);
    long startTime = ascentPoint + rampUpDelayForThread;
    long descentPoint = startTime + 1000 * flightTime + 1000 * startRampUp - rampUpDelayForThread;

    thread.setStartTime(startTime);
    thread.setEndTime(descentPoint + (int)Math.floor(1000 * endRampUp * this.threadsToSchedule / numThreads));

    thread.setScheduled(true);
    this.threadsToSchedule -= 1;
  }

  public JMeterProperty getData() {
    JMeterProperty brokenProp = getProperty("threads_schedule");
    JMeterProperty usualProp = getProperty("ultimatethreadgroupdata");

    if ((brokenProp instanceof CollectionProperty)) {
      if ((usualProp == null) || ((usualProp instanceof NullProperty))) {
        log.warn("Copying 'threads_schedule' into 'ultimatethreadgroupdata'");
        JMeterProperty newProp = brokenProp.clone();
        newProp.setName("ultimatethreadgroupdata");
        setProperty(newProp);
      }
      log.warn("Removing property 'threads_schedule' as invalid");
      removeProperty("threads_schedule");
    }

    CollectionProperty overrideProp = getLoadFromExternalProperty();
    if (overrideProp != null) {
      return overrideProp;
    }

    return getProperty("ultimatethreadgroupdata");
  }

  public void setData(CollectionProperty rows)
  {
    setProperty(rows);
  }

  private CollectionProperty getLoadFromExternalProperty()
  {
    String loadProp = JMeterUtils.getProperty("threads_schedule");
    log.debug("Profile prop: " + loadProp);
    if ((loadProp != null) && (loadProp.length() > 0))
    {
      log.info("GUI threads profile will be ignored");
      PowerTableModel dataModel = new PowerTableModel(UltimateThreadGroupGui.columnIdentifiers, UltimateThreadGroupGui.columnClasses);
      String[] chunks = loadProp.split("\\)");

      for (String chunk : chunks) {
        try {
          parseChunk(chunk, dataModel);
        } catch (RuntimeException e) {
          log.warn("Wrong  chunk ignored: " + chunk, e);
        }
      }

      log.info("Setting threads profile from property threads_schedule: " + loadProp);
      return JMeterPluginsUtils.tableModelRowsToCollectionProperty(dataModel, "ultimatethreadgroupdata");
    }
    return null;
  }

  private static void parseChunk(String chunk, PowerTableModel model) {
    log.debug("Parsing chunk: " + chunk);
    String[] parts = chunk.split("[(,]");
    String loadVar = parts[0].trim();

    if (loadVar.equalsIgnoreCase("spawn")) {
      Integer[] row = new Integer[5];
      row[0] = Integer.valueOf(Integer.parseInt(parts[1].trim()));
      row[1] = Integer.valueOf(JMeterPluginsUtils.getSecondsForShortString(parts[2]));
      row[2] = Integer.valueOf(JMeterPluginsUtils.getSecondsForShortString(parts[3]));
      row[3] = Integer.valueOf(JMeterPluginsUtils.getSecondsForShortString(parts[4]));
      row[4] = Integer.valueOf(JMeterPluginsUtils.getSecondsForShortString(parts[5]));
      model.addRow(row);
    } else {
      throw new RuntimeException("Unknown load type: " + parts[0]);
    }
  }

  public int getNumThreads()
  {
    int result = 0;

    JMeterProperty threadValues = getData();
    Iterator localIterator;
    if (!(threadValues instanceof NullProperty)) {
      CollectionProperty columns = (CollectionProperty)threadValues;
      List rows = (List)columns.getObjectValue();
      for (localIterator = rows.iterator(); localIterator.hasNext(); ) { Object row1 = localIterator.next();
        CollectionProperty prop = (CollectionProperty)row1;
        ArrayList row = (ArrayList)prop.getObjectValue();

        result += ((JMeterProperty)row.get(0)).getIntValue();
      }
    }

    return result;
  }

  public void testStarted()
  {
    JMeterProperty data = getData();
    if (!(data instanceof NullProperty)) {
      this.scheduleIT = ((CollectionProperty)data).iterator();
    }
    this.threadsToSchedule = 0;
  }

  public void testStarted(String host)
  {
    testStarted();
  }

  public void testEnded()
  {
  }

  public void testEnded(String host)
  {
    testEnded();
  }
}