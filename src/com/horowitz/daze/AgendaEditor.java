package com.horowitz.daze;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import org.apache.commons.lang.builder.CompareToBuilder;

import com.horowitz.commons.Settings;
import com.horowitz.daze.map.Agenda;
import com.horowitz.daze.map.AgendaEntry;
import com.horowitz.daze.map.DMap;
import com.horowitz.daze.map.JsonStorage;
import com.horowitz.daze.map.MapManager;
import com.horowitz.daze.map.Place;

public class AgendaEditor extends JPanel {

  private static final long serialVersionUID = -7306243578379329501L;
  private Box _box;
  private MapManager _mapManager;
  private JTextField _titleTF;
  private JTextField _entryIndexTF;

  public AgendaEditor(MapManager mapManager) {
    super(new BorderLayout());
    _mapManager = mapManager;
    initLayout();
  }

  private void initLayout() {
    JPanel mainRoot = new JPanel(new BorderLayout());
    JPanel headerPanel = new JPanel(new BorderLayout());
    // headerPanel.setBackground(Color.LIGHT_GRAY);
    headerPanel.setBorder(BorderFactory.createEmptyBorder(6, 3, 6, 3));
    // NAME
    _titleTF = new JTextField(15);
    _titleTF.setFont(_titleTF.getFont().deriveFont(18f));
    // _titleTF.setMargin(new Insets(3,3,3,3));
    // _titleTF.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(6,
    // 3, 6, 3),
    // _titleTF.getBorder()));
    // _titleTF.setBackground(Color.LIGHT_GRAY);
    JPanel titlePanel = new JPanel(new GridLayout(1, 2));
    titlePanel.add(_titleTF);
    _entryIndexTF = new JTextField(5);
    titlePanel.add(_entryIndexTF);
    headerPanel.add(titlePanel);

    // // fake label
    // gbc.gridy++;
    // gbc.gridx++;
    // gbc.weightx = 1.0;
    // gbc.weighty = 1.0;
    // headerPanel.add(new JLabel(""), gbc);
    mainRoot.add(headerPanel, BorderLayout.NORTH);

    // //////////
    // CENTER
    // //////////
    JPanel root = new JPanel(new BorderLayout());
    JScrollPane scrollPane = new JScrollPane(root);
    scrollPane.getVerticalScrollBar().setUnitIncrement(16);
    mainRoot.add(scrollPane, BorderLayout.CENTER);

    _box = Box.createVerticalBox();
    DragMouseAdapter dmAdapter = new DragMouseAdapter(JFrame.getFrames()[0]);
    _box.addMouseListener(dmAdapter);
    _box.addMouseMotionListener(dmAdapter);
    _box.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseReleased(MouseEvent e) {
        resetNumbers();
      }
    });
    mainRoot.setMinimumSize(new Dimension(50, 100));
    // mainRoot.setMaximumSize(new Dimension(100, 800));
    mainRoot.setPreferredSize(new Dimension(90, 420));

    _box.setMinimumSize(new Dimension(100, 20));
    // _box.setMaximumSize(new Dimension(200, 800));
    // _box.setPreferredSize(new Dimension(200, 280));
    root.add(_box, BorderLayout.NORTH);

    // ADD BUTTON
    Box tempBar = Box.createHorizontalBox();

    JButton addButton = new JButton(new AddAction());
    addButton.setMargin(new Insets(2, 2, 2, 2));
    shrinkFont(addButton, 9f);

    tempBar.setBorder(new EmptyBorder(3, 3, 3, 3));
    tempBar.add(addButton);
    tempBar.add(Box.createHorizontalStrut(6));

    JButton saveButton = new JButton(new AbstractAction("save") {

      @Override
      public void actionPerformed(ActionEvent e) {
        try {
          Agenda extractedAgenda = extractAgenda();
          List<Agenda> agendas = new ArrayList<Agenda>();
          agendas.add(extractedAgenda);
          new JsonStorage().saveAgendas(agendas);
        } catch (IOException e1) {
          e1.printStackTrace();
        }
      }
    });

    shrinkFont(saveButton, 9f);
    saveButton.setMargin(new Insets(2, 2, 2, 2));
    // tempBar.add(saveButton);
    // tempBar.add(Box.createHorizontalStrut(3));

    JButton loadButton = new JButton(new AbstractAction("load") {

      @Override
      public void actionPerformed(ActionEvent e) {
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            Agenda agenda = loadAgenda();
            applyAgenda(agenda);
            revalidate();

          }
        });
      }
    });
    shrinkFont(loadButton, 9f);
    loadButton.setMargin(new Insets(2, 2, 2, 2));
    // tempBar.add(loadButton);

    root.add(tempBar, BorderLayout.SOUTH);

    add(mainRoot, BorderLayout.NORTH);

    addRow();
  }

  public Agenda extractAgenda() {
    int n = _box.getComponentCount();
    Agenda egenda = new Agenda();
    List<AgendaEntry> entries = new ArrayList<AgendaEntry>();
    for (int i = 0; i < n; i++) {
      AgendaEntryView aev = (AgendaEntryView) _box.getComponent(i);
      DMap map = (DMap) aev._mapCB.getSelectedItem();
      Place place = (Place) aev._placeCB.getSelectedItem();
      AgendaEntry ae = new AgendaEntry();
      ae.setMapName(map.getName());
      ae.setWorldName(map.getWorld());
      ae.setPlaceName(place.getName());
      String dd = aev._directionsTF.getText();
      if (dd == null)
        dd = "NESW";
      if (dd.length() == 0)
        dd = "NESW";

      dd = dd.toUpperCase();

      String res = dd;
      if (dd.length() > 0 && dd.length() != 4) {
        res = "";
        String ss = "NESW";
        for (int j = 0; j < dd.length() && ss.length() > 0; j++) {
          String d = dd.substring(j, j + 1);
          res += d;
          int ind = ss.indexOf(d);
          if (ind >= 0)
            ss = ss.substring(0, ind) + ss.substring(ind + 1);
        }
        res += ss;
      }

      ae.setDirections(res);
      aev._directionsTF.setText(res);
      entries.add(ae);
    }
    egenda.setEntries(entries);
    egenda.setName(_titleTF.getText());
    return egenda;
  }

  public void applyAgenda(Agenda agenda) {
    _box.removeAll();
    if (agenda != null && agenda.getEntries() != null) {
      setVisible(true);
      _titleTF.setText(agenda.getName());
      for (AgendaEntry ae : agenda.getEntries()) {
        AgendaEntryView aev = new AgendaEntryView();
        ComboBoxModel<DMap> mapModel = aev._mapCB.getModel();
        for (int i = 0; i < mapModel.getSize(); i++) {
          if (mapModel.getElementAt(i).getName().equals(ae.getMapName())
              && mapModel.getElementAt(i).getWorld().equals(ae.getWorldName())) {
            aev._mapCB.setSelectedIndex(i);
            aev.loadPlaces(mapModel.getElementAt(i));
            break;
          }
        }
        ComboBoxModel<Place> placeModel = aev._placeCB.getModel();
        for (int i = 0; i < placeModel.getSize(); i++) {
          if (placeModel.getElementAt(i).getName().equals(ae.getPlaceName())) {
            aev._placeCB.setSelectedIndex(i);
            break;
          }
        }
        aev._directionsTF.setText(ae.getDirections());
        _box.add(aev);
      }
      resetNumbers();
    } else {
      setVisible(false);
    }
  }

  private void resetNumbers() {
    for(int i = 0; i < _box.getComponentCount(); i++) {
      Component comp = _box.getComponent(i);
      if (comp instanceof AgendaEntryView) {
        AgendaEntryView aev = (AgendaEntryView) comp;
        aev.setIndex(i);
      }
    }
  }

  private void addRow() {
    _box.add(new AgendaEntryView());
    resetNumbers();
  }

  void shrinkFont(Component comp, float size) {
    if (size < 0)
      size = comp.getFont().getSize() - 2;
    comp.setFont(comp.getFont().deriveFont(size));
  }

  class AgendaEntryView extends Box {

    private static final long serialVersionUID = -1413464832208854042L;

    RemoveAction _removeAction;
    DuplicateAction _duplicateAction;

    JComboBox<DMap> _mapCB;
    JComboBox<Place> _placeCB;
    JTextField _directionsTF;
    JLabel _anchor;

    public AgendaEntryView() {
      super(BoxLayout.LINE_AXIS);
      // remove
      _removeAction = new RemoveAction(AgendaEntryView.this);
      _duplicateAction = new DuplicateAction(AgendaEntryView.this);
      JButton removeButton = new JButton(_removeAction);
      shrinkFont(removeButton, -1);
      removeButton.setMargin(new Insets(2, 2, 2, 2));
      JButton duplicateButton = new JButton(_duplicateAction);
      shrinkFont(duplicateButton, -1);
      duplicateButton.setMargin(new Insets(2, 2, 2, 2));
      _anchor = new JLabel("- ");
      shrinkFont(_anchor, -4);
      add(_anchor);
      add(removeButton);
      add(duplicateButton);
      add(Box.createHorizontalStrut(6));
      // map
      List<DMap> maps = new ArrayList<DMap>(_mapManager.getMaps());
      Collections.sort(maps, new Comparator<DMap>() {
        @Override
        public int compare(DMap o1, DMap o2) {
          return new CompareToBuilder().append(o1.getPosition(), o2.getPosition()).toComparison();
        }
      });
      _mapCB = new JComboBox<DMap>(maps.toArray(new DMap[0]));
      shrinkFont(_mapCB, -4);
      add(_mapCB);
      add(Box.createHorizontalStrut(6));

      // Place
      _placeCB = new JComboBox<Place>();
      shrinkFont(_placeCB, -4);
      add(_placeCB);
      // dest
      _directionsTF = new JTextField(5);
      shrinkFont(_directionsTF, -4);

      add(_directionsTF);

      _mapCB.addItemListener(new ItemListener() {

        @Override
        public void itemStateChanged(ItemEvent e) {
          if (e.getStateChange() == ItemEvent.SELECTED) {
            final DMap map = (DMap) e.getItem();
            loadPlaces(map);
          }
        }

      });

      _mapCB.setSelectedIndex(-1);
      if (_mapCB.getModel().getSize() > 0)
        _mapCB.setSelectedIndex(0);

      add(Box.createHorizontalStrut(6));

      setBorder(new EmptyBorder(3, 3, 3, 0));
    }

    public void setIndex(int i) {
      _anchor.setText(i + " ");
    }

    protected void loadPlaces(DMap map) {
      final DefaultComboBoxModel<Place> model = (DefaultComboBoxModel<Place>) _placeCB.getModel();
      model.removeAllElements();
      for (Place place : map.getPlaces()) {
        model.addElement(place);
      }

    }

  }

  class AddAction extends AbstractAction {

    private static final long serialVersionUID = 3079504856636198124L;

    public AddAction() {
      super("  +  ");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {

          addRow();
          revalidate();
        }
      });

    }
  }

  class RemoveAction extends AbstractAction {
    private static final long serialVersionUID = -632649060095568382L;
    Component _comp;

    public RemoveAction(Component comp) {
      super(" - ");
      _comp = comp;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          _box.remove(_comp);
          resetNumbers();
          revalidate();
        }
      });

    }
  }

  class DuplicateAction extends AbstractAction {
    private static final long serialVersionUID = -632649060095568382L;
    Component _comp;

    public DuplicateAction(Component comp) {
      super("c");
      _comp = comp;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          // _box.remove(_comp);
          AgendaEntryView copy = new AgendaEntryView();
          AgendaEntryView src = (AgendaEntryView) _comp;
          copy._mapCB.setSelectedItem(src._mapCB.getSelectedItem());
          copy._placeCB.setSelectedItem(src._placeCB.getSelectedItem());
          copy._directionsTF.setText(src._directionsTF.getText());

          // find the position
          int pos = -1;
          for (int i = 0; i < _box.getComponentCount(); i++) {
            Component component = _box.getComponent(i);
            if (component == _comp) {
              pos = i;
              break;
            }
          }
          if (pos < 0)
            _box.add(copy);
          else
            _box.add(copy, pos + 1);

          resetNumbers();
          revalidate();
        }
      });

    }
  }

  private Agenda loadAgenda() {
    try {
      List<Agenda> agendas = new JsonStorage().loadAgendas();

      for (Agenda a : agendas) {
        if (a.getName().equals("noname"))
          return a;
      }

    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  public static void main(String[] args) {
    String dd = "W";
    String res = "";
    if (dd.length() > 0 && dd.length() != 4) {
      String ss = "NESW";
      for (int j = 0; j < dd.length() && ss.length() > 0; j++) {
        String d = dd.substring(j, j + 1);
        res += d;
        int ind = ss.indexOf(d);
        if (ind >= 0)
          ss = ss.substring(0, ind) + ss.substring(ind + 1);
      }
      res += ss;
    }
    System.out.println(res);

    try {
      JFrame frame = new JFrame("TEST");
      MapManager mapManager = new MapManager(new ScreenScanner(null), new Settings("daze.properties"));
      // mapManager.loadData();
      AgendaEditor panel = new AgendaEditor(mapManager);
      frame.getContentPane().add(panel);
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      frame.setBounds(400, 200, 500, 600);

      frame.setVisible(true);
    } catch (Exception e) {
      e.printStackTrace();
    }

  }

  class NoneSelectedButtonGroup extends ButtonGroup {

    @Override
    public void setSelected(ButtonModel model, boolean selected) {

      if (selected) {

        super.setSelected(model, selected);

      } else {

        clearSelection();
      }
    }
  }

  static class PlaceListModel extends DefaultComboBoxModel<Place> {

    private static final long serialVersionUID = 4516670732589078627L;

  }

  public void setEntryIndex(int i) {
    _entryIndexTF.setText("" + i);
  }
  
  public int getEntryIndex() {
    return Integer.parseInt(_entryIndexTF.getText());
  }

}
