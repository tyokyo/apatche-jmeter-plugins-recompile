package kg.apc.jmeter.threads;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.util.concurrent.ConcurrentHashMap;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableCellEditor;
import kg.apc.charting.AbstractGraphRow;
import kg.apc.charting.ChartSettings;
import kg.apc.charting.DateTimeRenderer;
import kg.apc.charting.GraphPanelChart;
import kg.apc.charting.rows.GraphRowSumValues;
import kg.apc.jmeter.JMeterPluginsUtils;
import kg.apc.jmeter.gui.ButtonPanelAddCopyRemove;
import kg.apc.jmeter.gui.GuiBuilderHelper;
import org.apache.jmeter.control.LoopController;
import org.apache.jmeter.control.gui.LoopControlPanel;
import org.apache.jmeter.gui.util.PowerTableModel;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.CollectionProperty;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.testelement.property.NullProperty;
import org.apache.jmeter.threads.JMeterThread;
import org.apache.jmeter.threads.gui.AbstractThreadGroupGui;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

public class UltimateThreadGroupGui extends AbstractThreadGroupGui
  implements TableModelListener, CellEditorListener
{
  public static final String WIKIPAGE = "UltimateThreadGroup";
  private static final Logger log = LoggingManager.getLoggerForClass();
  protected ConcurrentHashMap<String, AbstractGraphRow> model;
  private GraphPanelChart chart;
  public static final String[] columnIdentifiers = { "Start Threads Count", "Initial Delay, sec", "Startup Time, sec", "Hold Load For, sec", "Shutdown Time" };

  public static final Class[] columnClasses = { String.class, String.class, String.class, String.class, String.class };

  public static final Integer[] defaultValues = { 
    Integer.valueOf(100), 
    Integer.valueOf(0), Integer.valueOf(30), Integer.valueOf(60), Integer.valueOf(10) };
  private LoopControlPanel loopPanel;
  protected PowerTableModel tableModel;
  protected JTable grid;
  protected ButtonPanelAddCopyRemove buttons;

  public UltimateThreadGroupGui()
  {
    init();
  }

  protected final void init()
  {
    JMeterPluginsUtils.addHelpLinkToPanel(this, "UltimateThreadGroup");
    JPanel containerPanel = new VerticalPanel();

    containerPanel.add(createParamsPanel(), "North");
    containerPanel.add(GuiBuilderHelper.getComponentWithMargin(createChart(), 2, 2, 0, 2), "Center");
    add(containerPanel, "Center");

    createControllerPanel();
  }

  private JPanel createParamsPanel() {
    JPanel panel = new JPanel(new BorderLayout(5, 5));
    panel.setBorder(BorderFactory.createTitledBorder("Threads Schedule"));
    panel.setPreferredSize(new Dimension(200, 200));

    JScrollPane scroll = new JScrollPane(createGrid());
    scroll.setPreferredSize(scroll.getMinimumSize());
    panel.add(scroll, "Center");
    this.buttons = new ButtonPanelAddCopyRemove(this.grid, this.tableModel, defaultValues);
    panel.add(this.buttons, "South");

    return panel;
  }

  private JTable createGrid() {
    this.grid = new JTable();
    this.grid.getDefaultEditor(String.class).addCellEditorListener(this);
    createTableModel();
    this.grid.setSelectionMode(0);
    this.grid.setMinimumSize(new Dimension(200, 100));

    return this.grid;
  }

  public String getLabelResource()
  {
    return getClass().getSimpleName();
  }

  public String getStaticLabel()
  {
    return JMeterPluginsUtils.prefixLabel("Ultimate Thread Group");
  }

  public TestElement createTestElement()
  {
    UltimateThreadGroup tg = new UltimateThreadGroup();
    modifyTestElement(tg);
    tg.setComment(JMeterPluginsUtils.getWikiLinkText("UltimateThreadGroup"));

    return tg;
  }

  public void modifyTestElement(TestElement tg)
  {
    if (this.grid.isEditing()) {
      this.grid.getCellEditor().stopCellEditing();
    }

    if ((tg instanceof UltimateThreadGroup)) {
      UltimateThreadGroup utg = (UltimateThreadGroup)tg;
      CollectionProperty rows = JMeterPluginsUtils.tableModelRowsToCollectionProperty(this.tableModel, "ultimatethreadgroupdata");
      utg.setData(rows);
      utg.setSamplerController((LoopController)this.loopPanel.createTestElement());
    }
    super.configureTestElement(tg);
  }

  public void configure(TestElement tg)
  {
    super.configure(tg);
    UltimateThreadGroup utg = (UltimateThreadGroup)tg;

    JMeterProperty threadValues = utg.getData();
    if (!(threadValues instanceof NullProperty)) {
      CollectionProperty columns = (CollectionProperty)threadValues;

      this.tableModel.removeTableModelListener(this);
      JMeterPluginsUtils.collectionPropertyToTableModelRows(columns, this.tableModel);
      this.tableModel.addTableModelListener(this);
      updateUI();
    } else {
      log.warn("Received null property instead of collection");
    }

    TestElement te = (TestElement)tg.getProperty("ThreadGroup.main_controller").getObjectValue();
    if (te != null) {
      this.loopPanel.configure(te);
    }
    this.buttons.checkDeleteButtonStatus();
  }

  public void updateUI()
  {
    super.updateUI();

    if (this.tableModel != null) {
      UltimateThreadGroup utgForPreview = new UltimateThreadGroup();
      utgForPreview.setData(JMeterPluginsUtils.tableModelRowsToCollectionPropertyEval(this.tableModel, "ultimatethreadgroupdata"));
      updateChart(utgForPreview);
    }
  }

  private void updateChart(UltimateThreadGroup tg) {
    tg.testStarted();
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
    this.chart.setForcedMinX(now);

    row.add(now, 0.0D);

    int numThreads = tg.getNumThreads();
    log.debug("Num Threads: " + numThreads);
    for (int n = 0; n < numThreads; n++) {
      thread.setThreadNum(n);
      thread.setThreadName(Integer.toString(n));
      tg.scheduleThread(thread, now);
      row.add(thread.getStartTime() - 1L, 0.0D);
      row.add(thread.getStartTime(), 1.0D);
    }

    tg.testStarted();

    for (int n = 0; n < tg.getNumThreads(); n++) {
      thread.setThreadNum(n);
      thread.setThreadName(Integer.toString(n));
      tg.scheduleThread(thread, now);
      row.add(thread.getEndTime() - 1L, 0.0D);
      row.add(thread.getEndTime(), -1.0D);
    }

    this.model.put("Expected parallel users count", row);
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

  private Component createChart() {
    this.chart = new GraphPanelChart(false, true);
    this.model = new ConcurrentHashMap();
    this.chart.setRows(this.model);
    this.chart.getChartSettings().setDrawFinalZeroingLines(true);
    this.chart.setxAxisLabel("Elapsed time");
    this.chart.setYAxisLabel("Number of active threads");
    this.chart.setBorder(BorderFactory.createBevelBorder(1));
    return this.chart;
  }

  public void tableChanged(TableModelEvent e)
  {
    updateUI();
  }

  private void createTableModel() {
    this.tableModel = new PowerTableModel(columnIdentifiers, columnClasses);
    this.tableModel.addTableModelListener(this);
    this.grid.setModel(this.tableModel);
  }

  public void editingStopped(ChangeEvent e)
  {
    updateUI();
  }

  public void editingCanceled(ChangeEvent e)
  {
  }

  public void clearGui()
  {
    super.clearGui();
    this.tableModel.clearData();
  }
}