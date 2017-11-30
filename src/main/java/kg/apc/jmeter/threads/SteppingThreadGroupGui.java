package kg.apc.jmeter.threads;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.util.concurrent.ConcurrentHashMap;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import kg.apc.charting.AbstractGraphRow;
import kg.apc.charting.ChartSettings;
import kg.apc.charting.DateTimeRenderer;
import kg.apc.charting.GraphPanelChart;
import kg.apc.charting.rows.GraphRowSumValues;
import kg.apc.jmeter.JMeterPluginsUtils;
import kg.apc.jmeter.gui.GuiBuilderHelper;
import org.apache.jmeter.control.LoopController;
import org.apache.jmeter.control.gui.LoopControlPanel;
import org.apache.jmeter.engine.util.CompoundVariable;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.threads.JMeterThread;
import org.apache.jmeter.threads.gui.AbstractThreadGroupGui;
import org.apache.jorphan.collections.HashTree;

@Deprecated
public class SteppingThreadGroupGui extends AbstractThreadGroupGui
{
  public static final String WIKIPAGE = "SteppingThreadGroup";
  protected ConcurrentHashMap<String, AbstractGraphRow> model;
  private GraphPanelChart chart;
  private JTextField initialDelay;
  private JTextField incUserCount;
  private JTextField incUserCountBurst;
  private JTextField incUserPeriod;
  private JTextField flightTime;
  private JTextField decUserCount;
  private JTextField decUserPeriod;
  private JTextField totalThreads;
  private LoopControlPanel loopPanel;
  private JTextField rampUp;

  public SteppingThreadGroupGui()
  {
    init();
    initGui();
  }

  protected final void init() {
    JMeterPluginsUtils.addHelpLinkToPanel(this, "SteppingThreadGroup");
    JPanel containerPanel = new JPanel(new BorderLayout());

    containerPanel.add(createParamsPanel(), "North");

    this.chart = new GraphPanelChart(false, true);
    this.model = new ConcurrentHashMap();
    this.chart.setRows(this.model);
    this.chart.getChartSettings().setDrawFinalZeroingLines(true);

    this.chart.setxAxisLabel("Elapsed time");
    this.chart.setYAxisLabel("Number of active threads");

    this.chart.setBorder(BorderFactory.createBevelBorder(1));

    containerPanel.add(GuiBuilderHelper.getComponentWithMargin(this.chart, 2, 2, 0, 2), "Center");

    add(containerPanel, "Center");

    createControllerPanel();
  }

  public void clearGui()
  {
    super.clearGui();
    initGui();
  }

  private void initGui()
  {
    this.totalThreads.setText("100");
    this.initialDelay.setText("0");
    this.incUserCount.setText("10");
    this.incUserCountBurst.setText("0");
    this.incUserPeriod.setText("30");
    this.flightTime.setText("60");
    this.decUserCount.setText("5");
    this.decUserPeriod.setText("1");
    this.rampUp.setText("5");
  }

  private JPanel createParamsPanel() {
    JPanel panel = new JPanel(new GridLayout(0, 5, 5, 5));
    panel.setBorder(BorderFactory.createTitledBorder("Threads Scheduling Parameters"));

    panel.add(new JLabel("This group will start", 4));
    this.totalThreads = new JTextField(5);
    panel.add(this.totalThreads);
    panel.add(new JLabel("threads:", 2));
    panel.add(new JLabel());
    panel.add(new JLabel());

    panel.add(new JLabel("First, wait for", 4));
    this.initialDelay = new JTextField(5);
    panel.add(this.initialDelay);
    panel.add(new JLabel("seconds;", 2));
    panel.add(new JLabel());
    panel.add(new JLabel());

    panel.add(new JLabel("Then start", 4));
    this.incUserCountBurst = new JTextField(5);
    panel.add(this.incUserCountBurst);
    panel.add(new JLabel("threads; ", 2));
    panel.add(new JLabel(""));
    panel.add(new JLabel());

    panel.add(new JLabel("Next, add", 4));
    this.incUserCount = new JTextField(5);
    panel.add(this.incUserCount);
    panel.add(new JLabel("threads every", 0));
    this.incUserPeriod = new JTextField(5);
    panel.add(this.incUserPeriod);
    panel.add(new JLabel("seconds, ", 2));

    panel.add(new JLabel());
    panel.add(new JLabel());
    panel.add(new JLabel("using ramp-up", 4));
    this.rampUp = new JTextField(5);
    panel.add(this.rampUp);
    panel.add(new JLabel("seconds.", 2));

    panel.add(new JLabel("Then hold load for", 4));
    this.flightTime = new JTextField(5);
    panel.add(this.flightTime);
    panel.add(new JLabel("seconds.", 2));
    panel.add(new JLabel());
    panel.add(new JLabel());

    panel.add(new JLabel("Finally, stop", 4));
    this.decUserCount = new JTextField(5);
    panel.add(this.decUserCount);
    panel.add(new JLabel("threads every", 0));
    this.decUserPeriod = new JTextField(5);
    panel.add(this.decUserPeriod);
    panel.add(new JLabel("seconds.", 2));

    registerJTextfieldForGraphRefresh(this.totalThreads);
    registerJTextfieldForGraphRefresh(this.initialDelay);
    registerJTextfieldForGraphRefresh(this.incUserCount);
    registerJTextfieldForGraphRefresh(this.incUserCountBurst);
    registerJTextfieldForGraphRefresh(this.incUserPeriod);
    registerJTextfieldForGraphRefresh(this.flightTime);
    registerJTextfieldForGraphRefresh(this.decUserCount);
    registerJTextfieldForGraphRefresh(this.decUserPeriod);
    registerJTextfieldForGraphRefresh(this.rampUp);

    return panel;
  }

  public String getLabelResource()
  {
    return getClass().getSimpleName();
  }

  public String getStaticLabel()
  {
    return JMeterPluginsUtils.prefixLabel("Stepping Thread Group (deprecated)");
  }

  public TestElement createTestElement()
  {
    SteppingThreadGroup tg = new SteppingThreadGroup();
    modifyTestElement(tg);
    tg.setComment(JMeterPluginsUtils.getWikiLinkText("SteppingThreadGroup"));
    return tg;
  }

  private void refreshPreview() {
    SteppingThreadGroup tgForPreview = new SteppingThreadGroup();
    tgForPreview.setNumThreads(new CompoundVariable(this.totalThreads.getText()).execute());
    tgForPreview.setThreadGroupDelay(new CompoundVariable(this.initialDelay.getText()).execute());
    tgForPreview.setInUserCount(new CompoundVariable(this.incUserCount.getText()).execute());
    tgForPreview.setInUserCountBurst(new CompoundVariable(this.incUserCountBurst.getText()).execute());
    tgForPreview.setInUserPeriod(new CompoundVariable(this.incUserPeriod.getText()).execute());
    tgForPreview.setOutUserCount(new CompoundVariable(this.decUserCount.getText()).execute());
    tgForPreview.setOutUserPeriod(new CompoundVariable(this.decUserPeriod.getText()).execute());
    tgForPreview.setFlightTime(new CompoundVariable(this.flightTime.getText()).execute());
    tgForPreview.setRampUp(new CompoundVariable(this.rampUp.getText()).execute());

    if (tgForPreview.getInUserCountAsInt() == 0) {
      tgForPreview.setInUserCount(new CompoundVariable(this.totalThreads.getText()).execute());
    }
    if (tgForPreview.getOutUserCountAsInt() == 0) {
      tgForPreview.setOutUserCount(new CompoundVariable(this.totalThreads.getText()).execute());
    }

    updateChart(tgForPreview);
  }

  public void modifyTestElement(TestElement te)
  {
    super.configureTestElement(te);

    if ((te instanceof SteppingThreadGroup)) {
      SteppingThreadGroup tg = (SteppingThreadGroup)te;
      tg.setProperty("ThreadGroup.num_threads", this.totalThreads.getText());
      tg.setThreadGroupDelay(this.initialDelay.getText());
      tg.setInUserCount(this.incUserCount.getText());
      tg.setInUserCountBurst(this.incUserCountBurst.getText());
      tg.setInUserPeriod(this.incUserPeriod.getText());
      tg.setOutUserCount(this.decUserCount.getText());
      tg.setOutUserPeriod(this.decUserPeriod.getText());
      tg.setFlightTime(this.flightTime.getText());
      tg.setRampUp(this.rampUp.getText());
      tg.setSamplerController((LoopController)this.loopPanel.createTestElement());

      refreshPreview();
    }
  }

  public void configure(TestElement te)
  {
    super.configure(te);
    SteppingThreadGroup tg = (SteppingThreadGroup)te;
    this.totalThreads.setText(tg.getNumThreadsAsString());
    this.initialDelay.setText(tg.getThreadGroupDelay());
    this.incUserCount.setText(tg.getInUserCount());
    this.incUserCountBurst.setText(tg.getInUserCountBurst());
    this.incUserPeriod.setText(tg.getInUserPeriod());
    this.decUserCount.setText(tg.getOutUserCount());
    this.decUserPeriod.setText(tg.getOutUserPeriod());
    this.flightTime.setText(tg.getFlightTime());
    this.rampUp.setText(tg.getRampUp());

    TestElement controller = (TestElement)tg.getProperty("ThreadGroup.main_controller").getObjectValue();
    if (controller != null)
      this.loopPanel.configure(controller);
  }

  private void updateChart(SteppingThreadGroup tg)
  {
    this.model.clear();

    GraphRowSumValues row = new GraphRowSumValues();
    row.setColor(Color.RED);
    row.setDrawLine(true);
    row.setMarkerSize(0);
    row.setDrawThickLines(true);

    HashTree hashTree = new HashTree();
    hashTree.add(new LoopController());
    JMeterThread thread = new JMeterThread(hashTree, null, null);

    long now = System.currentTimeMillis();

    this.chart.setxAxisLabelRenderer(new DateTimeRenderer("HH:mm:ss", now - 1L));
    row.add(now, 0.0D);
    row.add(now + tg.getThreadGroupDelayAsInt(), 0.0D);

    int numThreads = tg.getNumThreads();

    for (int n = 0; n < numThreads; n++) {
      thread.setThreadNum(n);
      tg.scheduleThread(thread, now);
      row.add(thread.getStartTime() - 1L, 0.0D);
      row.add(thread.getStartTime(), 1.0D);
    }

    for (int n = 0; n < numThreads; n++) {
      thread.setThreadNum(n);
      tg.scheduleThread(thread, now);
      row.add(thread.getEndTime() - 1L, 0.0D);
      row.add(thread.getEndTime(), -1.0D);
    }

    this.model.put("Expected Active Users Count", row);
    this.chart.invalidateCache();
    this.chart.repaint();
  }

  private JPanel createControllerPanel() {
    this.loopPanel = new LoopControlPanel(false);
    LoopController looper = (LoopController)this.loopPanel.createTestElement();
    looper.setLoops(-1);
    looper.setContinueForever(true);
    this.loopPanel.configure(looper);
    return this.loopPanel;
  }

  private void registerJTextfieldForGraphRefresh(JTextField tf) {
    tf.addActionListener(this.loopPanel);
    tf.getDocument().addDocumentListener(new FieldChangesListener(tf));
  }

  private class FieldChangesListener
    implements DocumentListener
  {
    private final JTextField tf;

    public FieldChangesListener(JTextField field)
    {
      this.tf = field;
    }

    private void update() {
      SteppingThreadGroupGui.this.refreshPreview();
    }

    public void insertUpdate(DocumentEvent e)
    {
      if (this.tf.hasFocus())
        update();
    }

    public void removeUpdate(DocumentEvent e)
    {
      if (this.tf.hasFocus())
        update();
    }

    public void changedUpdate(DocumentEvent e)
    {
      if (this.tf.hasFocus())
        update();
    }
  }
}