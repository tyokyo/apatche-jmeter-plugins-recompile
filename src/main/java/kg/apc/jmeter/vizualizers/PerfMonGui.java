package kg.apc.jmeter.vizualizers;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListMap;
import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.text.Document;
import kg.apc.charting.AbstractGraphRow;
import kg.apc.charting.ChartSettings;
import kg.apc.charting.GraphPanelChart;
import kg.apc.jmeter.JMeterPluginsUtils;
import kg.apc.jmeter.graphs.AbstractOverTimeVisualizer;
import kg.apc.jmeter.graphs.GraphPanel;
import kg.apc.jmeter.gui.ButtonPanelAddCopyRemove;
import kg.apc.jmeter.gui.ComponentBorder;
import kg.apc.jmeter.gui.DialogFactory;
import kg.apc.jmeter.gui.GuiBuilderHelper;
import kg.apc.jmeter.perfmon.PerfMonCollector;
import kg.apc.jmeter.perfmon.PerfMonSampleResult;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.util.PowerTableModel;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.CollectionProperty;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.testelement.property.NullProperty;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

public class PerfMonGui extends AbstractOverTimeVisualizer
{
  public static final List<String> metrics = Arrays.asList(new String[] { "CPU", "Memory", "Swap", "Disks I/O", "Network I/O" });
  private static final Logger log = LoggingManager.getLoggerForClass();
  private PowerTableModel tableModel;
  private JTable grid;
  private JTextArea errorTextArea;
  private JScrollPane errorPane;
  public static final String[] columnIdentifiers = { "Host / IP", "Port", "Metric to collect", "Metric parameter (see help)" };

  public static final Class[] columnClasses = { String.class, String.class, String.class, String.class };

  private static String[] defaultValues = { "localhost", "4444", "CPU", "" };

  public PerfMonGui()
  {
    setGranulation(1000);
    this.graphPanel.getGraphObject().setYAxisLabel("Performance Metrics");
    this.graphPanel.getGraphObject().getChartSettings().setExpendRows(true);
  }

  protected JSettingsPanel createSettingsPanel()
  {
    return new JSettingsPanel(this, 13074);
  }

  public String getWikiPage()
  {
    return "PerfMon";
  }

  public String getLabelResource()
  {
    return getClass().getSimpleName();
  }

  public String getStaticLabel()
  {
    return JMeterPluginsUtils.prefixLabel("PerfMon Metrics Collector");
  }

  protected JPanel getGraphPanelContainer()
  {
    JPanel panel = new JPanel(new BorderLayout());
    JPanel innerTopPanel = new JPanel(new BorderLayout());

    this.errorPane = new JScrollPane();
    this.errorPane.setMinimumSize(new Dimension(100, 50));
    this.errorPane.setPreferredSize(new Dimension(100, 50));

    this.errorTextArea = new JTextArea();
    this.errorTextArea.setForeground(Color.red);
    this.errorTextArea.setBackground(new Color(255, 255, 153));
    this.errorTextArea.setEditable(false);
    this.errorPane.setViewportView(this.errorTextArea);

    registerPopup();

    innerTopPanel.add(createConnectionsPanel(), "North");
    innerTopPanel.add(this.errorPane, "South");
    innerTopPanel.add(getFilePanel(), "Center");

    panel.add(innerTopPanel, "North");

    this.errorPane.setVisible(false);

    return panel;
  }

  private void addErrorMessage(String msg, long time) {
    this.errorPane.setVisible(true);
    SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
    String newLine = "";
    if (this.errorTextArea.getText().length() != 0) {
      newLine = "\n";
    }
    this.errorTextArea.setText(this.errorTextArea.getText() + newLine + formatter.format(Long.valueOf(time)) + " - ERROR: " + msg);
    this.errorTextArea.setCaretPosition(this.errorTextArea.getDocument().getLength());
    updateGui();
  }

  public void clearErrorMessage() {
    this.errorTextArea.setText("");
    this.errorPane.setVisible(false);
  }

  private void registerPopup() {
    JPopupMenu popup = new JPopupMenu();
    JMenuItem hideMessagesMenu = new JMenuItem("Hide Error Panel");
    hideMessagesMenu.addActionListener(new HideAction());
    popup.add(hideMessagesMenu);
    this.errorTextArea.setComponentPopupMenu(popup);
  }

  public void clearData()
  {
    clearErrorMessage();
    super.clearData();
  }

  private Component createConnectionsPanel() {
    JPanel panel = new JPanel(new BorderLayout(5, 5));
    panel.setBorder(BorderFactory.createTitledBorder("Servers to Monitor (ServerAgent must be started, see help)"));
    panel.setPreferredSize(new Dimension(150, 150));

    JScrollPane scroll = new JScrollPane(createGrid());
    scroll.setPreferredSize(scroll.getMinimumSize());
    panel.add(scroll, "Center");
    panel.add(new ButtonPanelAddCopyRemove(this.grid, this.tableModel, defaultValues), "South");

    List items = new LinkedList(metrics);

    items.add("TCP");
    items.add("JMX");
    items.add("EXEC");
    items.add("TAIL");
    JComboBox metricTypesBox = new JComboBox(items.toArray());
    this.grid.getColumnModel().getColumn(2).setCellEditor(new DefaultCellEditor(metricTypesBox));

    final JTextField wizEditor = new JTextField();
    wizEditor.setBorder(null);
    JButton wiz = new JButton("...");
    if (!GraphicsEnvironment.isHeadless()) {
      wiz.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent evt) {
          Frame parent = GuiPackage.getInstance().getMainFrame();
          String type = PerfMonGui.this.grid.getValueAt(PerfMonGui.this.grid.getSelectedRow(), 2).toString();

          JPerfmonParamsPanel dlgContent = new JPerfmonParamsPanel(type, wizEditor);
          dlgContent.setMinWidth(400);
          JDialog dlg = DialogFactory.getJDialogInstance(parent, "PerfMon [" + type + "] Parameters Helper", true, dlgContent, "/kg/apc/jmeter/vizualizers/wand.png");

          DialogFactory.centerDialog(parent, dlg);

          dlg.setVisible(true);
        }
      });
    }
    wiz.setMargin(new Insets(0, 6, 5, 6));
    GuiBuilderHelper.strechItemToComponent(wizEditor, wiz);
    ComponentBorder bd = new ComponentBorder(wiz);

    bd.install(wizEditor);

    this.grid.getColumnModel().getColumn(3).setCellEditor(new DefaultCellEditor(wizEditor));
    this.grid.getTableHeader().setReorderingAllowed(false);

    return panel;
  }

  private JTable createGrid() {
    this.grid = new JTable();
    createTableModel();
    this.grid.setSelectionMode(0);
    this.grid.setMinimumSize(new Dimension(200, 100));

    this.grid.getColumnModel().getColumn(0).setPreferredWidth(170);
    this.grid.getColumnModel().getColumn(1).setPreferredWidth(80);
    this.grid.getColumnModel().getColumn(2).setPreferredWidth(120);
    this.grid.getColumnModel().getColumn(3).setPreferredWidth(500);

    return this.grid;
  }

  private void createTableModel() {
    this.tableModel = new PowerTableModel(columnIdentifiers, columnClasses);
    this.grid.setModel(this.tableModel);
  }

  public TestElement createTestElement()
  {
    TestElement te = new PerfMonCollector();
    modifyTestElement(te);
    te.setComment(JMeterPluginsUtils.getWikiLinkText(getWikiPage()));
    return te;
  }

  public void modifyTestElement(TestElement te)
  {
    super.modifyTestElement(te);
    if (this.grid.isEditing()) {
      this.grid.getCellEditor().stopCellEditing();
    }

    if ((te instanceof PerfMonCollector)) {
      PerfMonCollector pmte = (PerfMonCollector)te;
      CollectionProperty rows = JMeterPluginsUtils.tableModelRowsToCollectionProperty(this.tableModel, "metricConnections");
      pmte.setData(rows);
    }
    super.configureTestElement(te);
  }

  public void configure(TestElement te)
  {
    super.configure(te);
    PerfMonCollector pmte = (PerfMonCollector)te;
    JMeterProperty perfmonValues = pmte.getMetricSettings();
    if (!(perfmonValues instanceof NullProperty))
      JMeterPluginsUtils.collectionPropertyToTableModelRows((CollectionProperty)perfmonValues, this.tableModel);
    else
      log.warn("Received null property instead of collection");
  }

  public void add(SampleResult res)
  {
    if (res.isSuccessful()) {
      if (isSampleIncluded(res)) {
        super.add(res);
        addPerfMonRecord(res.getSampleLabel(), normalizeTime(res.getStartTime()), PerfMonSampleResult.getValue(res));
        updateGui(null);
      }
    }
    else addErrorMessage(res.getResponseMessage(), res.getStartTime());
  }

  private void addPerfMonRecord(String rowName, long time, double value)
  {
    AbstractGraphRow row = (AbstractGraphRow)this.model.get(rowName);
    if (row == null) {
      row = getNewRow(this.model, 0, rowName, 0, false, false, false, true, true);
    }

    row.add(time, value);
  }

  private class HideAction implements ActionListener {
    private HideAction() {
    }

    public void actionPerformed(ActionEvent e) {
      PerfMonGui.this.errorPane.setVisible(false);
      PerfMonGui.this.updateGui();
    }
  }
}