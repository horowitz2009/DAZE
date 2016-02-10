package com.horowitz.daze;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
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
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import org.apache.commons.lang.builder.CompareToBuilder;

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
    _titleTF = new JTextField(25);
    _titleTF.setFont(_titleTF.getFont().deriveFont(18f));
    // _titleTF.setMargin(new Insets(3,3,3,3));
    // _titleTF.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(6,
    // 3, 6, 3),
    // _titleTF.getBorder()));
    // _titleTF.setBackground(Color.LIGHT_GRAY);
    headerPanel.add(_titleTF);

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
    mainRoot.add(root, BorderLayout.CENTER);

    _box = Box.createVerticalBox();

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
      ae.setPlaceName(place.getName());
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
          if (mapModel.getElementAt(i).getName().equals(ae.getMapName())) {
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
        // aev._placeField.setText(ae.getPlaceName());
        _box.add(aev);
      }
    } else {
      setVisible(false);
    }
  }

  private void addRow() {
    _box.add(new AgendaEntryView());
  }

  void shrinkFont(Component comp, float size) {
    if (size < 0)
      size = comp.getFont().getSize() - 2;
    comp.setFont(comp.getFont().deriveFont(size));
  }

  class AgendaEntryView extends Box {

    private static final long serialVersionUID = -1413464832208854042L;

    RemoveAction _removeAction;

    JComboBox<DMap> _mapCB;
    JComboBox<Place> _placeCB;
    JTextField _placeField;

    public AgendaEntryView() {
      super(BoxLayout.LINE_AXIS);
      // remove
      _removeAction = new RemoveAction(AgendaEntryView.this);
      JButton removeButton = new JButton(_removeAction);
      shrinkFont(removeButton, -1);
      removeButton.setMargin(new Insets(2, 2, 2, 2));
      add(removeButton);
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
      shrinkFont(_mapCB, -1);
      add(_mapCB);
      add(Box.createHorizontalStrut(6));

      // Place
      _placeCB = new JComboBox<Place>();
      add(_placeCB);
      // dest
      _placeField = new JTextField(15);

      // add(_placeField);

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
      _mapCB.setSelectedIndex(0);

      add(Box.createHorizontalStrut(6));

      setBorder(new EmptyBorder(3, 3, 3, 0));
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
      super("  -  ");
      _comp = comp;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          _box.remove(_comp);
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

    try {
      JFrame frame = new JFrame("TEST");
      MapManager mapManager = new MapManager(new ScreenScanner(null));
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

}
