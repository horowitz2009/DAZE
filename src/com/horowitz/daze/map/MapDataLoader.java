package com.horowitz.daze.map;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.horowitz.commons.Pixel;

public class MapDataLoader {

  public static void main(String[] args) {
    File d = new File("storage/maps");

    if (!d.exists()) {
      d.mkdirs();
    }
    try {
      // AGENDA
      Agenda agenda1 = new Agenda("HMM");
      List<AgendaEntry> agendaEntries = new ArrayList<AgendaEntry>();
      //agendaEntries.add(new AgendaEntry("Repeatable", "Tin Cave"));
      //agendaEntries.add(new AgendaEntry("Hathor", "Closed Theatre"));
      //agendaEntries.add(new AgendaEntry("Strongmen", "Winter Cave Of Digging 2"));
      agenda1.setEntries(agendaEntries);
      List<Agenda> agendas = new ArrayList<Agenda>();
      agendas.add(agenda1);
      new JsonStorage().saveAgendas(agendas);

    } catch (IOException e) {
      e.printStackTrace();
    }

    try {
      // STRONGMEN 2 EVENT
      DMap map = new DMap("Strongmen");
      map.setPosition(-1);
      List<Place> places = new ArrayList<Place>();
      places.add(new Place("Winter Cave Of Digging 2", new Pixel(-75, -186), false));
      places.add(new Place("Winter Cave Of Rolling 2", new Pixel(310, -79), false));
      places.add(new Place("Winter Quiz Cave 2", new Pixel(711, -136), false));
      places.add(new Place("Winter Strongmen Chambers 2", new Pixel(434, -276), false));

      map.setPlaces(places);

      new JsonStorage().saveMap(map);
    } catch (IOException e) {
      e.printStackTrace();
    }

    try {
      // MAIN
      DMap map = new DMap("Main");
      map.setPosition(0);
      List<Place> places = new ArrayList<Place>();
      places.add(new Place("Pen-Stock", new Pixel(807, -394), false));
      places.add(new Place("Luxor Electrical Substation", new Pixel(943, -305), false));
      places.add(new Place("Endangered Oasis", new Pixel(788, -307), false));
      places.add(new Place("Machine Room", new Pixel(836, -179), false));
      // places.add(new Place("Luxor Town", new Pixel(645, -35), false));

      map.setPlaces(places);

      new JsonStorage().saveMap(map);
    } catch (IOException e) {
      e.printStackTrace();
    }

    try {
      // REPEATABLE
      DMap map = new DMap("Repeatable");
      map.setPosition(1);
      List<Place> places = new ArrayList<Place>();
      places.add(new Place("Smithy", new Pixel(-185, -33), true));
      places.add(new Place("Fishing Pond", new Pixel(-88, -189), true));

      places.add(new Place("Tutankhamun's Treasury", new Pixel(-304, -194), true));
      places.add(new Place("Lucky Oasis", new Pixel(-304, -324), true));
      places.add(new Place("Tin Cave", new Pixel(-149, -342), true));
      places.add(new Place("Coal Cavern", new Pixel(152, -331), true));
      places.add(new Place("Small Stone Pit", new Pixel(323, -379), true));
      places.add(new Place("Abandoned Tunnel", new Pixel(398, -173), true));
      places.add(new Place("Pyramid Of Eternal Knowledge", new Pixel(572, -316), true));
      places.add(new Place("Mushroom Pit", new Pixel(622, -190), true));
      places.add(new Place("Plantation", new Pixel(867, -316), true));
      places.add(new Place("Feline Treasury", new Pixel(939, -211), true));
      places.add(new Place("Stony Landslide", new Pixel(970, -410), true));
      places.add(new Place("Luxor Library", new Pixel(1046, -124), true));
      places.add(new Place("Iron Mine", new Pixel(1202, -31), true));
      places.add(new Place("Lumber Warehouse", new Pixel(1202, -205), true));

      map.setPlaces(places);

      new JsonStorage().saveMap(map);
    } catch (IOException e) {
      e.printStackTrace();
    }

    try {
      // HATHOR
      DMap map = new DMap("Hathor");
      map.setPosition(11);
      List<Place> places = new ArrayList<Place>();
      places.add(new Place("Closed Theatre", new Pixel(433, -59), true));
      places.add(new Place("Sandy Pastures", new Pixel(513, -182), true));
      places.add(new Place("Monestery Of Joy", new Pixel(585, -261), true));

      map.setPlaces(places);

      new JsonStorage().saveMap(map);
    } catch (IOException e) {
      e.printStackTrace();
    }
    System.out.println("DONE.");
  }

}
