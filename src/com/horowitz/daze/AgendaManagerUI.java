package com.horowitz.daze;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.horowitz.commons.Settings;
import com.horowitz.daze.map.Agenda;
import com.horowitz.daze.map.AgendaEntry;
import com.horowitz.daze.map.JsonStorage;
import com.horowitz.daze.map.MapManager;

public class AgendaManagerUI extends JPanel {

  private static final long serialVersionUID = -3068182629869865033L;

  private final static Logger LOGGER = Logger.getLogger("MAIN");

  private JList<Agenda> _agendasCB;
  private MapManager _mapManager;
  private AgendaEditor _editor;

  public AgendaManagerUI(MapManager mapManager) {
    super();
    _mapManager = mapManager;
    initLayout();
    initLayout2();
    // reload();
  }

  class MyListModel extends DefaultListModel<Agenda> {

    private static final long serialVersionUID = 4516670732589078627L;

    @Override
    public Agenda remove(int index) {
      return super.remove(index);
    }
  }

  private void initLayout() {
    setLayout(new BorderLayout());
    Box box = Box.createVerticalBox();
    JPanel headerPanel = new JPanel(new BorderLayout());
    JToolBar toolbar = new JToolBar();
    box.add(toolbar);
    headerPanel.add(box, BorderLayout.EAST);
    toolbar.setFloatable(false);

    // THE LIST
    _model = new MyListModel();
    _agendasCB = new JList<Agenda>(_model);
    _agendasCB.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    _agendasCB.setVisibleRowCount(3);
    // toolbar.add(new JScrollPane(_protocolsCB));
    headerPanel.add(new JScrollPane(_agendasCB), BorderLayout.CENTER);
    {
      JButton button = new JButton(new AbstractAction("New") {

        private static final long serialVersionUID = 1L;

        @Override
        public void actionPerformed(ActionEvent e) {
          Agenda newAgenda = new Agenda("agenda1");
          newAgenda.setEntries(new ArrayList<AgendaEntry>(2));
          _model.addElement(newAgenda);

          // _protocolsCB.getSelectionModel().setSelectionInterval(_model.getSize()
          // - 1, _model.getSize() - 1);
          _agendasCB.setSelectedValue(newAgenda, true);

        }
      });
      shrinkFont(button, -1);
      button.setMargin(new Insets(2, 2, 2, 2));

      toolbar.add(button);
    }
    {
      JButton button = new JButton(new AbstractAction("Delete") {

        private static final long serialVersionUID = 1L;

        @Override
        public void actionPerformed(ActionEvent e) {
          delete();
        }
      });
      shrinkFont(button, -1);
      button.setMargin(new Insets(2, 2, 2, 2));

      toolbar.add(button);
    }

    toolbar = new JToolBar();
    toolbar.setFloatable(false);
    box.add(toolbar);

    {
      JButton button = new JButton(new AbstractAction("Save") {

        private static final long serialVersionUID = 1L;

        @Override
        public void actionPerformed(ActionEvent e) {
          save();
        }
      });
      shrinkFont(button, -1);
      button.setMargin(new Insets(2, 2, 2, 2));

      toolbar.add(button);
    }
    {
      JButton button = new JButton(new AbstractAction("Reload") {

        private static final long serialVersionUID = 1L;

        @Override
        public void actionPerformed(ActionEvent e) {
          reload();
        }
      });
      shrinkFont(button, -1);
      button.setMargin(new Insets(2, 2, 2, 2));

      toolbar.add(button);
    }

    toolbar = new JToolBar();
    toolbar.setFloatable(false);
    box.add(toolbar);

    {
      JButton button = new JButton(new AbstractAction("Dup") {
        private static final long serialVersionUID = 1L;

        @Override
        public void actionPerformed(ActionEvent e) {
          duplicate();
        }
      });
      shrinkFont(button, -1);
      button.setMargin(new Insets(2, 2, 2, 2));

      toolbar.add(button);
    }
    {
      JButton button = new JButton(new AbstractAction("Reset") {

        private static final long serialVersionUID = 1L;

        @Override
        public void actionPerformed(ActionEvent e) {
          // try {
          // _mapManager.resetDispatchEntries();
          // } catch (IOException e1) {
          // LOGGER.info("Failed to reset entries!");
          // }
        }
      });
      shrinkFont(button, -1);
      button.setMargin(new Insets(2, 2, 2, 2));

      toolbar.add(button);
    }

    add(headerPanel, BorderLayout.NORTH);
  }

  private void initLayout2() {
    _editor = new AgendaEditor(_mapManager);
    _editor.setMinimumSize(new Dimension(300, 300));
    _editor.setPreferredSize(new Dimension(260, 480));
    // add(new JScrollPane(_editor), BorderLayout.CENTER);
    add(_editor, BorderLayout.CENTER);

    _agendasCB.addListSelectionListener(new ListSelectionListener() {

      @Override
      public void valueChanged(ListSelectionEvent e) {
        if (!e.getValueIsAdjusting()) {
          Agenda agenda = _agendasCB.getSelectedValue();
          _editor.applyAgenda(agenda);
          revalidate();
        }
      }
    });

  }

  private void shrinkFont(Component comp, float size) {
    if (size < 0)
      size = comp.getFont().getSize() - 2;
    comp.setFont(comp.getFont().deriveFont(size));
  }

  private MyListModel _model;

  private Agenda energyAgenda;

  public Agenda getEnergyAgenda() {
    return energyAgenda;
  }

  public void reload() {
    // SwingUtilities.invokeLater(new Runnable() {
    this.energyAgenda = null;
    // public void run() {
    try {
      _mapManager.loadMaps();
      JsonStorage js = new JsonStorage();
      List<Agenda> agendas = js.loadAgendas();
      _model.clear();

      for (Agenda agenda : agendas) {
        _model.addElement(agenda);
        if (agenda.getName().equals("ENERGY")) {
          this.energyAgenda = agenda;
        }
      }
      // revalidate();

    } catch (IOException e) {
      e.printStackTrace();
    }
    // }
    // });

  }

  private void duplicate() {
    SwingUtilities.invokeLater(new Runnable() {

      public void run() {
        Agenda newAgenda = _agendasCB.getSelectedValue();
        if (newAgenda != null) {
          try {
            newAgenda = (Agenda) newAgenda.clone();
            newAgenda.setName(newAgenda.getName() + " COPY");
            _model.addElement(newAgenda);
            _agendasCB.setSelectedValue(newAgenda, true);
          } catch (CloneNotSupportedException e) {
            e.printStackTrace();
          }

        }
        revalidate();

      }
    });

  }

  private void save() {
    SwingUtilities.invokeLater(new Runnable() {

      public void run() {
        try {
          JsonStorage js = new JsonStorage();
          Agenda editedAgenda = _editor.extractAgenda();
          Agenda originalAgenda = (Agenda) _agendasCB.getSelectedValue();

          originalAgenda.setName(editedAgenda.getName());
          originalAgenda.setEntries(editedAgenda.getEntries());

          Enumeration<Agenda> elements = _model.elements();
          List<Agenda> newList = new ArrayList<Agenda>();
          while (elements.hasMoreElements()) {
            Agenda agenda = (Agenda) elements.nextElement();
            newList.add(agenda);
          }

          js.saveAgendas(newList);
          // _protocolsCB.setSelectedValue(null, false);
          // _agendasCB.setSelectedIndex(-1);
          _agendasCB.getSelectionModel().clearSelection();
          _agendasCB.setSelectedValue(originalAgenda, true);
          revalidate();

        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    });

  }

  private void delete() {
    SwingUtilities.invokeLater(new Runnable() {

      public void run() {
        try {
          JsonStorage js = new JsonStorage();
          int index = _agendasCB.getSelectedIndex();
          _model.remove(index);

          Enumeration<Agenda> elements = _model.elements();
          List<Agenda> newList = new ArrayList<Agenda>();
          while (elements.hasMoreElements()) {
            Agenda agenda = (Agenda) elements.nextElement();
            newList.add(agenda);
          }

          js.saveAgendas(newList);
          revalidate();

        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    });

  }

  public static void main(String[] args) {
    try {
      JFrame frame = new JFrame("TEST");
      MapManager mapManager = new MapManager(new ScreenScanner(null), new Settings("daze.properties"));
      // mapManager.loadData();
      AgendaManagerUI panel = new AgendaManagerUI(mapManager);
      frame.getContentPane().add(panel);
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      frame.setBounds(400, 200, 400, 600);

      frame.setVisible(true);
    } catch (Exception e) {
      e.printStackTrace();
    }

  }

  public Agenda getSelectedAgenda() {
    return _agendasCB.getSelectedValue();
  }

  public void addListSelectionListener(ListSelectionListener listener) {
    _agendasCB.addListSelectionListener(listener);
  }

  public void setAgenda(String agendaName) {
    if (agendaName == null)
      agendaName = "DEFAULT";
    int index = -1;
    for (int i = 0; i < _agendasCB.getModel().getSize(); i++) {
      Agenda ag = _agendasCB.getModel().getElementAt(i);
      if (ag.getName().equals(agendaName)) {
        index = i;
        _agendasCB.setSelectedIndex(index);
        break;
      }
    }

  }

  public void setEntryIndex(int i) {
    _editor.setEntryIndex(i);
  }
  
  public int getEntryIndex() {
    return _editor.getEntryIndex();
  }

}
