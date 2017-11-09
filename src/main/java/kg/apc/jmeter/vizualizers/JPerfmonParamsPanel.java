package kg.apc.jmeter.vizualizers;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.Border;
import kg.apc.jmeter.JMeterPluginsUtils;
import kg.apc.jmeter.gui.JAbsrtactDialogPanel;

public class JPerfmonParamsPanel extends JAbsrtactDialogPanel
{
  private static final int OPTION_PRIMARY_METRIC = 1;
  private static final int OPTION_ADDITIONAL_METRIC = 2;
  private static final int OPTION_PROCESS_SCOPE = 4;
  private static final int OPTION_CPU_CORE_SCOPE = 8;
  private static final int OPTION_FILESYSTEM_SCOPE = 16;
  private static final int OPTION_NET_INTERFACE_SCOPE = 32;
  private static final int OPTION_EXEC = 64;
  private static final int OPTION_TAIL = 128;
  private static final int OPTION_JMX = 256;
  private static final int OPTION_UNIT = 512;
  private JTextField parent = null;
  private String type = null;
  private static final String defaultMarker = " (default)";
  private static final String separator = ":";
  private static final String METRIC_CPU = "CPU";
  private static final String METRIC_MEM = "Memory";
  private static final String METRIC_SWAP = "Swap";
  private static final String METRIC_DISKIO = "Disks I/O";
  private static final String METRIC_NETIO = "Network I/O";
  private static final String METRIC_TCP = "TCP";
  private static final String METRIC_EXEC = "EXEC";
  private static final String METRIC_TAIL = "TAIL";
  private static final String METRIC_JMX = "JMX";
  private HashMap<String, Integer> rules = new HashMap();

  private ArrayList<String> unitRules = new ArrayList();

  private static String[] cpuMetricsPrimary = { "combined (default)", "Get the combined CPU usage, in percent (%)", "idle", "Get the idle CPU usage, in percent (%)", "system", "Get the system CPU usage, in percent (%)", "user", "Get the user CPU usage, in percent (%)", "iowait", "Get the iowait CPU usage, in percent (%)" };

  private static String[] cpuProcessMetricsPrimary = { "percent (default)", "Get the process combined CPU usage, in percent (%)", "total", "Get the process cpu time (sum of User and System) per second, in ms", "system", "Get the process cpu kernel time per second, in ms", "user", "Get the process cpu user time per second, in ms" };

  private static String[] cpuMetricsAdditional = { "irq", "Get the irq CPU usage, in percent (%)", "nice", "Get the nice CPU usage, in percent (%)", "softirq", "Get the softirq CPU usage, in percent (%)", "stolen", "Get the stolen CPU usage, in percent (%)" };

  private static String[] memMetricsPrimary = { "usedperc (default)", "Relative memory usage, in percent (%)", "freeperc", "Relative free memory, in percent (%)", "used", "Size of Memory used", "free", "Size of Free memory" };

  private static String[] memMetricsAdditional = { "actualused", "Size of Actual memory usage", "actualfree", "Size of Actual free memory", "ram", "Server physical memory in Mb", "total", "Size of Total memory" };

  private static String[] memProcessMetricsPrimary = { "resident (default)", "Size of Process resident memory usage", "virtual", "Size of Process virtual memory usage", "shared", "Size of Process shared memory usage" };

  private static String[] memProcessMetricsAdditional = { "pagefaults", "Process page faults count", "majorfaults", "Process major faults count", "minorfaults", "Process minor faults count" };

  private static String[] diskIOMetricsPrimary = { "queue (default)", "Description to update", "reads", "Number of read access", "writes", "Number of write access", "readbytes", "Number of bytes read", "writebytes", "Number of bytes written" };

  private static String[] diskIOMetricsAdditional = { "available", "Description to update", "service", "Description to update", "files", "Description to update", "free", "Description to update", "freefiles", "Description to update", "total", "Description to update", "useperc", "Description to update", "used", "Description to update" };

  private static String[] netIOMetricsPrimary = { "bytesrecv (default)", "Number of bytes received", "bytessent", "Number of bytes sent", "rx", "Description to update", "tx", "Description to update" };

  private static String[] netIOMetricsAdditional = { "used", "Description to update", "speed", "Description to update", "rxdrops", "Description to update", "rxerr", "Description to update", "rxframe", "Description to update", "rxoverruns", "Description to update", "txcarrier", "Description to update", "txcollisions", "Description to update", "txdrops", "Description to update", "txerr", "Description to update", "txoverruns", "Description to update" };

  private static String[] tcpMetricsPrimary = { "estab (default)", "Number of established connections", "time_wait", "Description to update", "close_wait", "Description to update" };

  private static String[] tcpMetricsAdditional = { "bound", "Description to update", "close", "Description to update", "closing", "Description to update", "fin_wait1", "Description to update", "fin_wait2", "Description to update", "idle", "Description to update", "inbound", "Description to update", "last_ack", "Description to update", "listen", "Description to update", "outbound", "Description to update", "syn_recv", "Description to update" };

  private static String[] swapMetricsPrimary = { "used (default)", "Size of used swap", "pagein", "Number of page in", "pageout", "Number of page out", "free", "Size of free swap", "total", "Size of total swap" };

  private static String[] jmxMetricsPrimary = { "gc-time", "Time spent in garbage collection, milliseconds", "memory-usage", "Heap memory used by VM, bytes", "memory-committed", "Heap memory committed by VM, bytes", "memorypool-usage", "Heap memory pool usage, bytes", "memorypool-committed", "Heap memory pool committed size, bytes", "class-count", "Loaded class count in VM", "compile-time", "Time spent in compilation, milliseconds" };
  private ButtonGroup buttonGroupCpuCores;
  private ButtonGroup buttonGroupMetrics;
  private ButtonGroup buttonGroupPID;
  private ButtonGroup buttonGroupScope;
  private JButton jButtonApply;
  private JButton jButtonCancel;
  private JComboBox jComboBoxUnit;
  private JLabel jLabel1;
  private JLabel jLabel2;
  private JLabel jLabel3;
  private JLabel jLabel4;
  private JLabel jLabel5;
  private JLabel jLabel6;
  private JLabel jLabel7;
  private JLabel jLabelExec;
  private JLabel jLabelFileSystem;
  private JLabel jLabelMetricLabel;
  private JLabel jLabelNetInterface;
  private JLabel jLabelOccurence;
  private JLabel jLabelPtqlHelp;
  private JLabel jLabelTail;
  private JLabel jLabelUnit;
  private JPanel jPanelAdditionaMetrics;
  private JPanel jPanelButtons;
  private JPanel jPanelCpuCore;
  private JPanel jPanelCustomCommand;
  private JPanel jPanelFileSystem;
  private JPanel jPanelJmxParams;
  private JPanel jPanelMetricLabel;
  private JPanel jPanelNetInterface;
  private JPanel jPanelPID;
  private JPanel jPanelPrimaryMetrics;
  private JPanel jPanelScope;
  private JPanel jPanelStretch;
  private JPanel jPanelTailCommand;
  private JPanel jPanelUnit;
  private JRadioButton jRadioCpuAllCores;
  private JRadioButton jRadioCustomCpuCore;
  private JRadioButton jRadioPID;
  private JRadioButton jRadioProcessName;
  private JRadioButton jRadioPtql;
  private JRadioButton jRadioScopeAll;
  private JRadioButton jRadioScopePerProcess;
  private JScrollPane jScrollPane1;
  private JTextArea jTextAreaExecHelp;
  private JTextField jTextFieldCoreIndex;
  private JTextField jTextFieldExec;
  private JTextField jTextFieldFileSystem;
  private JTextField jTextFieldJmxHost;
  private JTextField jTextFieldJmxPassword;
  private JTextField jTextFieldJmxPort;
  private JTextField jTextFieldJmxUser;
  private JTextField jTextFieldMetricLabel;
  private JTextField jTextFieldNetInterface;
  private JTextField jTextFieldOccurence;
  private JTextField jTextFieldPID;
  private JTextField jTextFieldPorcessName;
  private JTextField jTextFieldPtql;
  private JTextField jTextFieldTail;

  public JPerfmonParamsPanel(String type, JTextField parentField)
  {
    this.parent = parentField;
    this.type = type;
    initRules();
    initUnitRules();
    initComponents();
    initMetrics(type);
    showProcessScopePanels();
    makePtqlLink();
    initFields();
  }

  private void addUnitRule(String type, String metric) {
    if (metric.endsWith(" (default)")) metric = metric.substring(0, metric.length() - " (default)".length());
    this.unitRules.add(new StringBuilder().append(type).append(metric).toString());
  }

  private void initUnitRules()
  {
    addUnitRule("Memory", memMetricsPrimary[4]);
    addUnitRule("Memory", memMetricsPrimary[6]);
    addUnitRule("Memory", memMetricsAdditional[0]);
    addUnitRule("Memory", memMetricsAdditional[2]);
    addUnitRule("Memory", memMetricsAdditional[6]);

    addUnitRule("Memory", memProcessMetricsPrimary[0]);
    addUnitRule("Memory", memProcessMetricsPrimary[2]);
    addUnitRule("Memory", memProcessMetricsPrimary[4]);

    addUnitRule("Swap", swapMetricsPrimary[0]);
    addUnitRule("Swap", swapMetricsPrimary[6]);
    addUnitRule("Swap", swapMetricsPrimary[8]);

    addUnitRule("Disks I/O", diskIOMetricsPrimary[6]);
    addUnitRule("Disks I/O", diskIOMetricsPrimary[8]);

    addUnitRule("Disks I/O", diskIOMetricsAdditional[0]);
    addUnitRule("Disks I/O", diskIOMetricsAdditional[4]);
    addUnitRule("Disks I/O", diskIOMetricsAdditional[6]);
    addUnitRule("Disks I/O", diskIOMetricsAdditional[8]);
    addUnitRule("Disks I/O", diskIOMetricsAdditional[10]);
    addUnitRule("Disks I/O", diskIOMetricsAdditional[14]);

    addUnitRule("Network I/O", netIOMetricsPrimary[0]);
    addUnitRule("Network I/O", netIOMetricsPrimary[2]);

    addUnitRule("JMX", jmxMetricsPrimary[2]);
    addUnitRule("JMX", jmxMetricsPrimary[4]);
    addUnitRule("JMX", jmxMetricsPrimary[6]);
    addUnitRule("JMX", jmxMetricsPrimary[8]);
  }

  private boolean isUnitRelevent(String metric) {
    return this.unitRules.contains(new StringBuilder().append(this.type).append(metric).toString());
  }

  private void enableUnit(String metric) {
    boolean show = isUnitRelevent(metric);
    this.jLabelUnit.setEnabled(show);
    this.jComboBoxUnit.setEnabled(show);
  }

  private String getUnit() {
    int unit = this.jComboBoxUnit.getSelectedIndex();
    switch (unit) {
    case 1:
      return "kb";
    case 2:
      return "mb";
    }
    return "b";
  }

  private void setUnit(String unit)
  {
    if ("kb".equalsIgnoreCase(unit)) this.jComboBoxUnit.setSelectedIndex(1);
    else if ("mb".equalsIgnoreCase(unit)) this.jComboBoxUnit.setSelectedIndex(2); else
      this.jComboBoxUnit.setSelectedIndex(0);
  }

  private String extractExecTailCmd(String params)
  {
    String[] tmp = params.split("(?<!\\\\):");
    String labelString = null;
    for (String aTmp : tmp)
      if (aTmp.startsWith("label="))
        labelString = aTmp;
    String ret;
    if (labelString != null)
    {
      if (params.startsWith(labelString))
        ret = params.substring(labelString.length() + ":".length());
      else
        ret = params.substring(0, params.indexOf(labelString) - ":".length());
    }
    else {
      ret = params;
    }

    return ret;
  }

  private void initFields() {
    String existing = this.parent.getText();

    String[] elements = existing.split("(?<!\\\\):");
    checkProcessScope(existing, elements);
    checkCPUCore(elements);
    checkFilesystem(elements);
    checkNetInterface(elements);
    checkJMX(elements);

    if ("EXEC".equals(this.type)) {
      this.jTextFieldExec.setText(extractExecTailCmd(existing));

      this.jPanelStretch.setVisible(false);
    } else if ("TAIL".equals(this.type)) {
      this.jTextFieldTail.setText(extractExecTailCmd(existing));
    } else {
      for (String element : elements) {
        initMetricRadios(element);
      }

    }

    int i = 0;
    while (i < elements.length) {
      if (elements[i].startsWith("label=")) {
        this.jTextFieldMetricLabel.setText(elements[i].substring(6));
        break;
      }
      i++;
    }

    i = 0;
    while (i < elements.length) {
      if (elements[i].startsWith("unit=")) {
        setUnit(elements[i].substring(5));
        break;
      }
      i++;
    }
  }

  private void checkNetInterface(String[] elements)
  {
    if ("Network I/O".equals(this.type)) {
      int i = 0;
      while (i < elements.length) {
        if (elements[i].startsWith("iface=")) {
          this.jTextFieldNetInterface.setText(elements[i].substring(6));
          break;
        }
        i++;
      }
    }
  }

  private void checkFilesystem(String[] elements)
  {
    if ("Disks I/O".equals(this.type)) {
      int i = 0;
      while (i < elements.length) {
        if (elements[i].startsWith("fs=")) {
          this.jTextFieldFileSystem.setText(elements[i].substring(3));
          break;
        }
        i++;
      }
    }
  }

  private void checkJMX(String[] elements)
  {
    if ("JMX".equals(this.type)) {
      int i = 0;
      while (i < elements.length) {
        if (elements[i].startsWith("url=")) {
          String[] tmp = elements[i].substring(4).split("\\\\:");
          this.jTextFieldJmxHost.setText(tmp[0]);
          if (tmp.length > 1) {
            this.jTextFieldJmxPort.setText(tmp[1]);
          }
        }
        if (elements[i].startsWith("user=")) {
          this.jTextFieldJmxUser.setText(elements[i].substring(5));
        }
        if (elements[i].startsWith("password=")) {
          this.jTextFieldJmxPassword.setText(elements[i].substring(9));
        }
        i++;
      }
    }
  }

  private void checkCPUCore(String[] elements)
  {
    if ("CPU".equals(this.type)) {
      int i = 0;
      while (i < elements.length) {
        if (elements[i].startsWith("core=")) {
          this.jRadioCustomCpuCore.setSelected(true);
          String[] tmp = elements[i].split("=");
          if (tmp.length > 1) {
            this.jTextFieldCoreIndex.setText(tmp[1]); break;
          }
          this.jTextFieldCoreIndex.setText("0");

          break;
        }
        i++;
      }
    }
  }

  private void checkProcessScope(String existing, String[] elements)
  {
    if (("CPU".equals(this.type)) || ("Memory".equals(this.type))) {
      if ((existing.contains("pid=")) || 
        (existing
        .contains("name=")) || 
        (existing
        .contains("ptql=")))
      {
        this.jRadioScopePerProcess.setSelected(true);
        showProcessScopePanels();
      }
      int i = 0;
      while (i < elements.length) {
        if (elements[i].startsWith("pid=")) {
          this.jRadioPID.setSelected(true);
          this.jTextFieldPID.setText(elements[i].substring(4));
          break;
        }if (elements[i].startsWith("name=")) {
          String[] tmp = elements[i].split("#");
          this.jRadioProcessName.setSelected(true);
          this.jTextFieldPorcessName.setText(tmp[0].substring(5));
          if (tmp.length != 2) break; this.jTextFieldOccurence.setText(tmp[1]); break;
        }
        if (elements[i].startsWith("ptql=")) {
          this.jRadioPtql.setSelected(true);
          this.jTextFieldPtql.setText(elements[i].substring(5));
          break;
        }
        i++;
      }
    }
  }

  private void initMetricRadios(String metricName) {
    Enumeration enu = this.buttonGroupMetrics.getElements();
    while (enu.hasMoreElements()) {
      JRadioButton radio = (JRadioButton)enu.nextElement();
      if (metricName.equals(radio.getActionCommand())) {
        radio.setSelected(true);
        enableUnit(metricName);
      }
    }
  }

  private void makePtqlLink() {
    this.jLabelPtqlHelp.setForeground(Color.blue);
    this.jLabelPtqlHelp.setFont(this.jLabelPtqlHelp.getFont().deriveFont(0));
    this.jLabelPtqlHelp.setCursor(new Cursor(12));
    this.jLabelPtqlHelp.addMouseListener(new MouseAdapter()
    {
      public void mouseClicked(MouseEvent e)
      {
        if ((e.getModifiers() & 0x10) == 16)
          JMeterPluginsUtils.openInBrowser("http://support.hyperic.com/display/SIGAR/PTQL");
      }
    });
    Border border = BorderFactory.createMatteBorder(0, 0, 1, 0, Color.blue);
    this.jLabelPtqlHelp.setBorder(border);
  }

  private void initRules() {
    this.rules.put("CPU", Integer.valueOf(15));
    this.rules.put("Memory", Integer.valueOf(519));
    this.rules.put("Swap", Integer.valueOf(513));
    this.rules.put("Disks I/O", Integer.valueOf(531));
    this.rules.put("Network I/O", Integer.valueOf(547));
    this.rules.put("TCP", Integer.valueOf(3));
    this.rules.put("EXEC", Integer.valueOf(64));
    this.rules.put("TAIL", Integer.valueOf(128));
    this.rules.put("JMX", Integer.valueOf(769));
  }

  private void fillMetrics(String[] metrics, JPanel panel) {
    if (metrics != null) {
      MetricActionListener listener = new MetricActionListener();
      for (int i = 0; i < metrics.length / 2; i++) {
        JRadioButton radio = new JRadioButton(metrics[(2 * i)]);
        String action = metrics[(2 * i)];
        if (action.endsWith(" (default)")) {
          action = action.substring(0, action.length() - " (default)".length());
        }
        radio.setActionCommand(action);
        radio.setToolTipText(metrics[(2 * i + 1)]);
        radio.addActionListener(listener);
        this.buttonGroupMetrics.add(radio);
        panel.add(radio);
      }
    } else {
      panel.add(new JLabel(" None..."));
    }
  }

  private void initMetrics(String metricType)
  {
    String[] primaryMetrics = null;
    String[] additionalMetrics = null;
    if (this.type.equals("CPU")) {
      primaryMetrics = cpuMetricsPrimary;
      additionalMetrics = cpuMetricsAdditional;
    } else if (this.type.equals("Memory")) {
      primaryMetrics = memMetricsPrimary;
      additionalMetrics = memMetricsAdditional;
    } else if (this.type.equals("Disks I/O")) {
      primaryMetrics = diskIOMetricsPrimary;
      additionalMetrics = diskIOMetricsAdditional;
    } else if (this.type.equals("Network I/O")) {
      primaryMetrics = netIOMetricsPrimary;
      additionalMetrics = netIOMetricsAdditional;
    } else if (this.type.equals("TCP")) {
      primaryMetrics = tcpMetricsPrimary;
      additionalMetrics = tcpMetricsAdditional;
    } else if (this.type.equals("Swap")) {
      primaryMetrics = swapMetricsPrimary;
      additionalMetrics = null;
    } else if (this.type.equals("JMX")) {
      primaryMetrics = jmxMetricsPrimary;
      additionalMetrics = null;
    }

    if (this.rules.containsKey(metricType))
    {
      if (((((Integer)this.rules.get(metricType)).intValue() & 0x1) != 0) && 
        ((((Integer)this.rules
        .get(metricType))
        .intValue() & 0x2) == 0))
        this.jPanelPrimaryMetrics.setLayout(new GridLayout(0, 2));
      else {
        this.jPanelPrimaryMetrics.setLayout(new GridLayout(0, 1));
      }

      this.jPanelScope.setVisible((((Integer)this.rules.get(metricType)).intValue() & 0x4) != 0);
      this.jPanelPID.setVisible((((Integer)this.rules.get(metricType)).intValue() & 0x4) != 0);
      if ((((Integer)this.rules.get(metricType)).intValue() & 0x1) != 0)
        fillMetrics(primaryMetrics, this.jPanelPrimaryMetrics);
      else {
        this.jPanelPrimaryMetrics.setVisible(false);
      }
      if ((((Integer)this.rules.get(metricType)).intValue() & 0x2) != 0)
        fillMetrics(additionalMetrics, this.jPanelAdditionaMetrics);
      else {
        this.jPanelAdditionaMetrics.setVisible(false);
      }
      this.jPanelCustomCommand.setVisible((((Integer)this.rules.get(metricType)).intValue() & 0x40) != 0);
      this.jPanelCpuCore.setVisible((((Integer)this.rules.get(metricType)).intValue() & 0x8) != 0);
      this.jPanelTailCommand.setVisible((((Integer)this.rules.get(metricType)).intValue() & 0x80) != 0);
      this.jPanelFileSystem.setVisible((((Integer)this.rules.get(metricType)).intValue() & 0x10) != 0);
      this.jPanelNetInterface.setVisible((((Integer)this.rules.get(metricType)).intValue() & 0x20) != 0);
      this.jPanelJmxParams.setVisible((((Integer)this.rules.get(metricType)).intValue() & 0x100) != 0);
      this.jPanelUnit.setVisible((((Integer)this.rules.get(metricType)).intValue() & 0x200) != 0);
    }
  }

  private String getProcessScopeString() {
    String ret = "";
    if (this.jRadioScopePerProcess.isSelected()) {
      if (this.buttonGroupPID.getSelection() != null) {
        String tmp = this.buttonGroupPID.getSelection().getActionCommand();
        if ("pid".equals(tmp)) {
          ret = new StringBuilder().append(ret).append("pid=").append(this.jTextFieldPID.getText().trim()).toString();
        } else if ("name".equals(tmp)) {
          String name = this.jTextFieldPorcessName.getText().trim();
          if (name.length() == 0) {
            name = "unknown";
          }
          ret = new StringBuilder().append(ret).append("name=").append(name).append("#").append(this.jTextFieldOccurence.getText().trim()).toString();
        } else if ("ptql".equals(tmp)) {
          String query = this.jTextFieldPtql.getText().trim();
          if (query.length() == 0) {
            query = "query";
          }
          ret = new StringBuilder().append(ret).append("ptql=").append(query).toString();
        }
      } else {
        ret = new StringBuilder().append(ret).append("pid=0").toString();
      }
    }
    return ret;
  }

  private String getJmxParamsString() {
    StringBuilder ret = new StringBuilder("");

    String host = this.jTextFieldJmxHost.getText().trim();
    String port = this.jTextFieldJmxPort.getText().trim();

    if ((!port.isEmpty()) && (host.isEmpty())) {
      host = "localhost";
    }

    String user = this.jTextFieldJmxUser.getText().trim();
    String password = this.jTextFieldJmxPassword.getText().trim();

    if (host.length() > 0) {
      String url = new StringBuilder().append("url=").append(host).toString();
      if (!port.isEmpty()) {
        url = new StringBuilder().append(url).append("\\:").append(port).toString();
      }
      addStringItem(ret, url);
    }

    if ((user.length() != 0) && (password.length() != 0)) {
      addStringItem(ret, new StringBuilder().append("user=").append(user).toString());
      addStringItem(ret, new StringBuilder().append("password=").append(password).toString());
    }

    return ret.toString();
  }

  private void addStringItem(StringBuilder buf, String item) {
    if ((item != null) && (item.length() > 0)) {
      if (buf.length() > 0) buf.append(":");
      buf.append(item);
    }
  }

  private String getParamsString() {
    StringBuilder ret = new StringBuilder("");

    if (this.type.equals("CPU")) {
      if (this.buttonGroupCpuCores.getSelection() != null) {
        String tmp = this.buttonGroupCpuCores.getSelection().getActionCommand();
        if ("index".equals(tmp)) {
          addStringItem(ret, new StringBuilder().append("core=").append(this.jTextFieldCoreIndex.getText().trim()).toString());
        }
      }
      addStringItem(ret, getProcessScopeString());
    } else if (this.type.equals("Memory")) {
      addStringItem(ret, getProcessScopeString());
    } else if (this.type.equals("Network I/O")) {
      String tmp = this.jTextFieldNetInterface.getText();
      if (tmp.trim().length() > 0)
        addStringItem(ret, new StringBuilder().append("iface=").append(tmp.trim()).toString());
    }
    else if ((!this.type.equals("TCP")) && 
      (!this.type.equals("Swap")) && 
      (this.type.equals("JMX"))) {
      addStringItem(ret, getJmxParamsString());
    }

    String tmp = this.jTextFieldMetricLabel.getText();
    if (tmp.trim().length() > 0) {
      addStringItem(ret, new StringBuilder().append("label=").append(tmp.trim()).toString());
    }

    if ((this.buttonGroupMetrics.getSelection() != null) && 
      (isUnitRelevent(this.buttonGroupMetrics.getSelection().getActionCommand()))) {
      String unit = getUnit();
      if (!"b".equalsIgnoreCase(unit)) {
        addStringItem(ret, new StringBuilder().append("unit=").append(getUnit()).toString());
      }

    }

    if (this.type.equals("EXEC"))
      addStringItem(ret, this.jTextFieldExec.getText().trim());
    else if (this.type.equals("TAIL")) {
      addStringItem(ret, this.jTextFieldTail.getText().trim());
    }

    if (this.buttonGroupMetrics.getSelection() != null) {
      addStringItem(ret, this.buttonGroupMetrics.getSelection().getActionCommand());
    }

    if (this.type.equals("Disks I/O")) {
      tmp = this.jTextFieldFileSystem.getText();
      if (tmp.trim().length() > 0) {
        addStringItem(ret, new StringBuilder().append("fs=").append(tmp.trim()).toString());
      }
    }

    return ret.toString();
  }

  private void initComponents()
  {
    this.buttonGroupPID = new ButtonGroup();
    this.buttonGroupMetrics = new ButtonGroup();
    this.buttonGroupCpuCores = new ButtonGroup();
    this.buttonGroupScope = new ButtonGroup();
    this.jPanelPID = new JPanel();
    this.jRadioPID = new JRadioButton();
    this.jTextFieldPID = new JTextField();
    this.jRadioProcessName = new JRadioButton();
    this.jTextFieldPorcessName = new JTextField();
    this.jLabelOccurence = new JLabel();
    this.jTextFieldOccurence = new JTextField();
    this.jRadioPtql = new JRadioButton();
    this.jTextFieldPtql = new JTextField();
    this.jLabel2 = new JLabel();
    this.jLabelPtqlHelp = new JLabel();
    this.jPanelButtons = new JPanel();
    this.jButtonApply = new JButton();
    this.jButtonCancel = new JButton();
    this.jPanelPrimaryMetrics = new JPanel();
    this.jPanelAdditionaMetrics = new JPanel();
    this.jPanelCustomCommand = new JPanel();
    this.jLabelExec = new JLabel();
    this.jTextFieldExec = new JTextField();
    this.jLabel1 = new JLabel();
    this.jScrollPane1 = new JScrollPane();
    this.jTextAreaExecHelp = new JTextArea();
    this.jPanelCpuCore = new JPanel();
    this.jRadioCpuAllCores = new JRadioButton();
    this.jRadioCustomCpuCore = new JRadioButton();
    this.jTextFieldCoreIndex = new JTextField();
    this.jPanelScope = new JPanel();
    this.jRadioScopeAll = new JRadioButton();
    this.jRadioScopePerProcess = new JRadioButton();
    this.jPanelTailCommand = new JPanel();
    this.jLabelTail = new JLabel();
    this.jTextFieldTail = new JTextField();
    this.jPanelFileSystem = new JPanel();
    this.jLabelFileSystem = new JLabel();
    this.jTextFieldFileSystem = new JTextField();
    this.jPanelNetInterface = new JPanel();
    this.jLabelNetInterface = new JLabel();
    this.jTextFieldNetInterface = new JTextField();
    this.jPanelMetricLabel = new JPanel();
    this.jLabelMetricLabel = new JLabel();
    this.jTextFieldMetricLabel = new JTextField();
    this.jPanelStretch = new JPanel();
    this.jPanelJmxParams = new JPanel();
    this.jLabel3 = new JLabel();
    this.jTextFieldJmxHost = new JTextField();
    this.jLabel4 = new JLabel();
    this.jTextFieldJmxPort = new JTextField();
    this.jLabel5 = new JLabel();
    this.jTextFieldJmxUser = new JTextField();
    this.jLabel6 = new JLabel();
    this.jTextFieldJmxPassword = new JTextField();
    this.jLabel7 = new JLabel();
    this.jPanelUnit = new JPanel();
    this.jLabelUnit = new JLabel();
    this.jComboBoxUnit = new JComboBox();

    setLayout(new GridBagLayout());

    this.jPanelPID.setBorder(BorderFactory.createTitledBorder("Process Identification"));
    this.jPanelPID.setLayout(new GridBagLayout());

    this.buttonGroupPID.add(this.jRadioPID);
    this.jRadioPID.setText("Process ID");
    this.jRadioPID.setActionCommand("pid");
    GridBagConstraints gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = 2;
    this.jPanelPID.add(this.jRadioPID, gridBagConstraints);

    this.jTextFieldPID.setMinimumSize(new Dimension(60, 20));
    this.jTextFieldPID.setPreferredSize(new Dimension(60, 20));
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 1;
    this.jPanelPID.add(this.jTextFieldPID, gridBagConstraints);

    this.buttonGroupPID.add(this.jRadioProcessName);
    this.jRadioProcessName.setText("Process Name");
    this.jRadioProcessName.setActionCommand("name");
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.fill = 2;
    this.jPanelPID.add(this.jRadioProcessName, gridBagConstraints);

    this.jTextFieldPorcessName.setMinimumSize(new Dimension(60, 20));
    this.jTextFieldPorcessName.setPreferredSize(new Dimension(60, 20));
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 2;
    this.jPanelPID.add(this.jTextFieldPorcessName, gridBagConstraints);

    this.jLabelOccurence.setText(", occurence: ");
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 2;
    this.jPanelPID.add(this.jLabelOccurence, gridBagConstraints);

    this.jTextFieldOccurence.setMinimumSize(new Dimension(60, 20));
    this.jTextFieldOccurence.setPreferredSize(new Dimension(60, 20));
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 3;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.anchor = 17;
    gridBagConstraints.weightx = 1.0D;
    this.jPanelPID.add(this.jTextFieldOccurence, gridBagConstraints);

    this.buttonGroupPID.add(this.jRadioPtql);
    this.jRadioPtql.setText("PTQL Query");
    this.jRadioPtql.setActionCommand("ptql");
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.fill = 2;
    this.jPanelPID.add(this.jRadioPtql, gridBagConstraints);
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.gridwidth = 4;
    gridBagConstraints.fill = 2;
    this.jPanelPID.add(this.jTextFieldPtql, gridBagConstraints);

    this.jLabel2.setIcon(new ImageIcon(getClass().getResource("/kg/apc/jmeter/vizualizers/information.png")));
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 5;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.insets = new Insets(0, 3, 0, 3);
    this.jPanelPID.add(this.jLabel2, gridBagConstraints);

    this.jLabelPtqlHelp.setText("Help");
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 6;
    gridBagConstraints.gridy = 3;
    this.jPanelPID.add(this.jLabelPtqlHelp, gridBagConstraints);

    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.fill = 2;
    add(this.jPanelPID, gridBagConstraints);

    this.jButtonApply.setIcon(new ImageIcon(getClass().getResource("/kg/apc/jmeter/vizualizers/tick.png")));
    this.jButtonApply.setText("Apply");
    this.jButtonApply.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        JPerfmonParamsPanel.this.jButtonApplyActionPerformed(evt);
      }
    });
    this.jPanelButtons.add(this.jButtonApply);

    this.jButtonCancel.setIcon(new ImageIcon(getClass().getResource("/kg/apc/jmeter/vizualizers/cross.png")));
    this.jButtonCancel.setText("Cancel");
    this.jButtonCancel.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        JPerfmonParamsPanel.this.jButtonCancelActionPerformed(evt);
      }
    });
    this.jPanelButtons.add(this.jButtonCancel);

    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 13;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.fill = 2;
    gridBagConstraints.insets = new Insets(15, 0, 0, 0);
    add(this.jPanelButtons, gridBagConstraints);

    this.jPanelPrimaryMetrics.setBorder(BorderFactory.createTitledBorder("Primary Metrics"));
    this.jPanelPrimaryMetrics.setLayout(new GridLayout(0, 1));
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.fill = 1;
    gridBagConstraints.weightx = 1.0D;
    add(this.jPanelPrimaryMetrics, gridBagConstraints);

    this.jPanelAdditionaMetrics.setBorder(BorderFactory.createTitledBorder("Additional Metrics"));
    this.jPanelAdditionaMetrics.setLayout(new GridLayout(0, 2));
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.fill = 1;
    gridBagConstraints.weightx = 1.0D;
    add(this.jPanelAdditionaMetrics, gridBagConstraints);

    this.jPanelCustomCommand.setBorder(BorderFactory.createTitledBorder("Custom Exec Command"));
    this.jPanelCustomCommand.setLayout(new GridBagLayout());

    this.jLabelExec.setText("Command to run:");
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.anchor = 17;
    this.jPanelCustomCommand.add(this.jLabelExec, gridBagConstraints);
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = 2;
    gridBagConstraints.weightx = 1.0D;
    gridBagConstraints.insets = new Insets(2, 0, 0, 0);
    this.jPanelCustomCommand.add(this.jTextFieldExec, gridBagConstraints);

    this.jLabel1.setIcon(new ImageIcon(getClass().getResource("/kg/apc/jmeter/vizualizers/information.png")));
    this.jLabel1.setText("Quick help:");
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.anchor = 17;
    gridBagConstraints.insets = new Insets(5, 0, 5, 0);
    this.jPanelCustomCommand.add(this.jLabel1, gridBagConstraints);

    this.jTextAreaExecHelp.setColumns(20);
    this.jTextAreaExecHelp.setEditable(false);
    this.jTextAreaExecHelp.setLineWrap(true);
    this.jTextAreaExecHelp.setRows(15);
    this.jTextAreaExecHelp.setText("This metric type interprets parameter string as path to process to start and arguments to pass to the process. Parameters separated with colon.\nThe process must print out to standard output single line containing single numeric metric value.\n\nExample1: Monitoring Linux cached memory size, used free utility output:\n/bin/sh:-c:free | grep Mem | awk '{print $7}'\n\nExample2: Monitoring MySQL select query count:\n/bin/sh:-c:echo \"show global status like 'Com_select'\" | mysql -u root | awk '$1 ==\"Com_select\" {print $2}'");
    this.jTextAreaExecHelp.setWrapStyleWord(true);
    this.jTextAreaExecHelp.setOpaque(false);
    this.jScrollPane1.setViewportView(this.jTextAreaExecHelp);

    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.fill = 1;
    gridBagConstraints.weighty = 1.0D;
    this.jPanelCustomCommand.add(this.jScrollPane1, gridBagConstraints);

    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 5;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.fill = 1;
    gridBagConstraints.weightx = 1.0D;
    gridBagConstraints.weighty = 1.0D;
    add(this.jPanelCustomCommand, gridBagConstraints);

    this.jPanelCpuCore.setBorder(BorderFactory.createTitledBorder("CPU Cores"));
    this.jPanelCpuCore.setLayout(new GridBagLayout());

    this.buttonGroupCpuCores.add(this.jRadioCpuAllCores);
    this.jRadioCpuAllCores.setSelected(true);
    this.jRadioCpuAllCores.setText("All Cores (default)");
    this.jRadioCpuAllCores.setActionCommand("all");
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.fill = 2;
    gridBagConstraints.anchor = 17;
    this.jPanelCpuCore.add(this.jRadioCpuAllCores, gridBagConstraints);

    this.buttonGroupCpuCores.add(this.jRadioCustomCpuCore);
    this.jRadioCustomCpuCore.setText("Custom CPU Index (0 based)");
    this.jRadioCustomCpuCore.setActionCommand("index");
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = 17;
    this.jPanelCpuCore.add(this.jRadioCustomCpuCore, gridBagConstraints);

    this.jTextFieldCoreIndex.setMinimumSize(new Dimension(60, 20));
    this.jTextFieldCoreIndex.setPreferredSize(new Dimension(60, 20));
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = 17;
    gridBagConstraints.weightx = 1.0D;
    this.jPanelCpuCore.add(this.jTextFieldCoreIndex, gridBagConstraints);

    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 4;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.fill = 2;
    add(this.jPanelCpuCore, gridBagConstraints);

    this.jPanelScope.setBorder(BorderFactory.createTitledBorder("Scope"));
    this.jPanelScope.setLayout(new GridLayout(0, 1));

    this.buttonGroupScope.add(this.jRadioScopeAll);
    this.jRadioScopeAll.setSelected(true);
    this.jRadioScopeAll.setText("All");
    this.jRadioScopeAll.setActionCommand("all");
    this.jRadioScopeAll.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        JPerfmonParamsPanel.this.jRadioScopeAllActionPerformed(evt);
      }
    });
    this.jPanelScope.add(this.jRadioScopeAll);

    this.buttonGroupScope.add(this.jRadioScopePerProcess);
    this.jRadioScopePerProcess.setText("Per Process");
    this.jRadioScopePerProcess.setActionCommand("process");
    this.jRadioScopePerProcess.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        JPerfmonParamsPanel.this.jRadioScopePerProcessActionPerformed(evt);
      }
    });
    this.jPanelScope.add(this.jRadioScopePerProcess);

    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.fill = 2;
    add(this.jPanelScope, gridBagConstraints);

    this.jPanelTailCommand.setBorder(BorderFactory.createTitledBorder("Custom Tail Command"));
    this.jPanelTailCommand.setLayout(new GridBagLayout());

    this.jLabelTail.setText("Path of the file to tail:");
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.anchor = 17;
    this.jPanelTailCommand.add(this.jLabelTail, gridBagConstraints);
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = 2;
    gridBagConstraints.weightx = 1.0D;
    gridBagConstraints.insets = new Insets(2, 0, 0, 0);
    this.jPanelTailCommand.add(this.jTextFieldTail, gridBagConstraints);

    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 6;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.fill = 2;
    gridBagConstraints.weightx = 1.0D;
    add(this.jPanelTailCommand, gridBagConstraints);

    this.jPanelFileSystem.setBorder(BorderFactory.createTitledBorder("Filesystem Filter"));
    this.jPanelFileSystem.setLayout(new GridBagLayout());

    this.jLabelFileSystem.setText("Filesystem to monitor (if empty: all), eg \"C\\:\\\" or \"/home\":");
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.anchor = 17;
    this.jPanelFileSystem.add(this.jLabelFileSystem, gridBagConstraints);
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = 2;
    gridBagConstraints.weightx = 1.0D;
    gridBagConstraints.insets = new Insets(2, 0, 0, 0);
    this.jPanelFileSystem.add(this.jTextFieldFileSystem, gridBagConstraints);

    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 7;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.fill = 2;
    gridBagConstraints.weightx = 1.0D;
    add(this.jPanelFileSystem, gridBagConstraints);

    this.jPanelNetInterface.setBorder(BorderFactory.createTitledBorder("Network Interface Filter"));
    this.jPanelNetInterface.setLayout(new GridBagLayout());

    this.jLabelNetInterface.setText("Network interface to monitor (if empty: all), eg \"eth0\":");
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.anchor = 17;
    this.jPanelNetInterface.add(this.jLabelNetInterface, gridBagConstraints);
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = 2;
    gridBagConstraints.weightx = 1.0D;
    gridBagConstraints.insets = new Insets(2, 0, 0, 0);
    this.jPanelNetInterface.add(this.jTextFieldNetInterface, gridBagConstraints);

    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 8;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.fill = 2;
    gridBagConstraints.weightx = 1.0D;
    add(this.jPanelNetInterface, gridBagConstraints);

    this.jPanelMetricLabel.setBorder(BorderFactory.createTitledBorder("Metric Label"));
    this.jPanelMetricLabel.setLayout(new GridBagLayout());

    this.jLabelMetricLabel.setText("Chart label name (if empty, will be 'Metric parameter' value):");
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.anchor = 17;
    this.jPanelMetricLabel.add(this.jLabelMetricLabel, gridBagConstraints);
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = 2;
    gridBagConstraints.weightx = 1.0D;
    gridBagConstraints.insets = new Insets(2, 0, 0, 0);
    this.jPanelMetricLabel.add(this.jTextFieldMetricLabel, gridBagConstraints);

    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 11;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.fill = 2;
    add(this.jPanelMetricLabel, gridBagConstraints);

    this.jPanelStretch.setMinimumSize(new Dimension(0, 0));
    this.jPanelStretch.setPreferredSize(new Dimension(0, 0));
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 12;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.fill = 1;
    gridBagConstraints.weighty = 1.0D;
    add(this.jPanelStretch, gridBagConstraints);

    this.jPanelJmxParams.setBorder(BorderFactory.createTitledBorder("JMX Connection Parameters"));
    this.jPanelJmxParams.setLayout(new GridBagLayout());

    this.jLabel3.setText("Host:");
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = 13;
    this.jPanelJmxParams.add(this.jLabel3, gridBagConstraints);

    this.jTextFieldJmxHost.setPreferredSize(new Dimension(60, 20));
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = 2;
    gridBagConstraints.weightx = 1.0D;
    gridBagConstraints.insets = new Insets(2, 2, 2, 2);
    this.jPanelJmxParams.add(this.jTextFieldJmxHost, gridBagConstraints);

    this.jLabel4.setText("Port:");
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = 13;
    this.jPanelJmxParams.add(this.jLabel4, gridBagConstraints);

    this.jTextFieldJmxPort.setPreferredSize(new Dimension(60, 20));
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 3;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = 2;
    gridBagConstraints.weightx = 1.0D;
    gridBagConstraints.insets = new Insets(2, 2, 2, 2);
    this.jPanelJmxParams.add(this.jTextFieldJmxPort, gridBagConstraints);

    this.jLabel5.setText("User:");
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.anchor = 13;
    this.jPanelJmxParams.add(this.jLabel5, gridBagConstraints);

    this.jTextFieldJmxUser.setPreferredSize(new Dimension(60, 20));
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.fill = 2;
    gridBagConstraints.insets = new Insets(2, 2, 2, 2);
    this.jPanelJmxParams.add(this.jTextFieldJmxUser, gridBagConstraints);

    this.jLabel6.setText("Password:");
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.anchor = 13;
    this.jPanelJmxParams.add(this.jLabel6, gridBagConstraints);

    this.jTextFieldJmxPassword.setPreferredSize(new Dimension(60, 20));
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 3;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.fill = 2;
    gridBagConstraints.insets = new Insets(2, 2, 2, 2);
    this.jPanelJmxParams.add(this.jTextFieldJmxPassword, gridBagConstraints);

    this.jLabel7.setIcon(new ImageIcon(getClass().getResource("/kg/apc/jmeter/vizualizers/information.png")));
    this.jLabel7.setText("If no host/port specified, local to agent on port 4711");
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.gridwidth = 4;
    gridBagConstraints.fill = 2;
    gridBagConstraints.insets = new Insets(4, 0, 2, 0);
    this.jPanelJmxParams.add(this.jLabel7, gridBagConstraints);

    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 9;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.fill = 1;
    add(this.jPanelJmxParams, gridBagConstraints);

    this.jPanelUnit.setBorder(BorderFactory.createTitledBorder("Metric Unit"));
    this.jPanelUnit.setLayout(new GridBagLayout());

    this.jLabelUnit.setText("Retrieve metric from agent in:");
    this.jLabelUnit.setEnabled(false);
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.anchor = 17;
    gridBagConstraints.insets = new Insets(0, 2, 2, 2);
    this.jPanelUnit.add(this.jLabelUnit, gridBagConstraints);

    this.jComboBoxUnit.setModel(new DefaultComboBoxModel(new String[] { "Bytes (b)", "Kilobytes (Kb)", "Megabytes (Mb)" }));
    this.jComboBoxUnit.setEnabled(false);
    this.jComboBoxUnit.setMaximumSize(new Dimension(32767, 20));
    this.jComboBoxUnit.setPreferredSize(new Dimension(130, 20));
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.anchor = 17;
    gridBagConstraints.weightx = 1.0D;
    gridBagConstraints.insets = new Insets(0, 2, 2, 2);
    this.jPanelUnit.add(this.jComboBoxUnit, gridBagConstraints);

    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 10;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.fill = 1;
    add(this.jPanelUnit, gridBagConstraints);
  }

  private void jButtonCancelActionPerformed(ActionEvent evt) {
    getAssociatedDialog().dispose();
  }

  private void jButtonApplyActionPerformed(ActionEvent evt) {
    this.parent.setText(getParamsString());
    getAssociatedDialog().dispose();
  }

  private void showProcessScopePanels() {
    if ((this.rules.containsKey(this.type)) && ((((Integer)this.rules.get(this.type)).intValue() & 0x4) != 0)) {
      this.jPanelPID.setVisible(this.jRadioScopePerProcess.isSelected());
      this.jPanelPrimaryMetrics.removeAll();
      this.jPanelAdditionaMetrics.removeAll();
      Enumeration enu = this.buttonGroupMetrics.getElements();
      while (enu.hasMoreElements()) {
        this.buttonGroupMetrics.remove((AbstractButton)enu.nextElement());
      }
      String[] primaryMetrics = null;
      String[] additionalMetrics = null;

      if (this.jRadioScopePerProcess.isSelected()) {
        if (this.type.equals("CPU")) {
          primaryMetrics = cpuProcessMetricsPrimary;
        } else if (this.type.equals("Memory")) {
          primaryMetrics = memProcessMetricsPrimary;
          additionalMetrics = memProcessMetricsAdditional;
        }
      }
      else if (this.type.equals("CPU")) {
        primaryMetrics = cpuMetricsPrimary;
        additionalMetrics = cpuMetricsAdditional;
      } else if (this.type.equals("Memory")) {
        primaryMetrics = memMetricsPrimary;
        additionalMetrics = memMetricsAdditional;
      }

      fillMetrics(primaryMetrics, this.jPanelPrimaryMetrics);
      fillMetrics(additionalMetrics, this.jPanelAdditionaMetrics);

      this.jLabelUnit.setEnabled(false);
      this.jComboBoxUnit.setEnabled(false);
    }
    repack();
  }

  private void jRadioScopeAllActionPerformed(ActionEvent evt) {
    showProcessScopePanels();
  }

  private void jRadioScopePerProcessActionPerformed(ActionEvent evt) {
    showProcessScopePanels();
  }
  private class MetricActionListener implements ActionListener {
    private MetricActionListener() {
    }
    public void actionPerformed(ActionEvent e) {
      JPerfmonParamsPanel.this.enableUnit(e.getActionCommand());
    }
  }
}