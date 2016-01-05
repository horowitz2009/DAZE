package com.horowitz.daze;

import java.awt.AWTException;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import Catalano.Core.IntRange;
import Catalano.Imaging.FastBitmap;
import Catalano.Imaging.Filters.ColorFiltering;
import Catalano.Imaging.Filters.ExtractRGBChannel;
import Catalano.Imaging.Filters.ExtractRGBChannel.Channel;
import Catalano.Imaging.Filters.Threshold;
import Catalano.Imaging.Tools.Blob;

import com.horowitz.commons.ImageComparator;
import com.horowitz.commons.ImageData;
import com.horowitz.commons.MotionDetector;
import com.horowitz.commons.MouseRobot;
import com.horowitz.commons.Pixel;
import com.horowitz.commons.RobotInterruptedException;
import com.horowitz.commons.SimilarityImageComparator;

public class BFSMazeRunner {

	private final static Logger LOGGER = Logger.getLogger("MAIN");

	private ScreenScanner _scanner;
	private MouseRobot _mouse;
	private Map<Point, Position> _matrix2;
	private List<Position> _matrix;

	private ImageComparator _comparator;

	private List<Position> _searchSequence;

	private ColorFiltering _greenColorFiltering = new ColorFiltering(new IntRange(70, 140), new IntRange(110, 255),
	new IntRange(0, 65));

	public BFSMazeRunner(ScreenScanner scanner) {
		super();
		_scanner = scanner;
		_mouse = _scanner.getMouse();
		// _comparator = _scanner.getComparator();
		_comparator = new SimilarityImageComparator(0.04, 15000);
		_comparator.setErrors(4);
		_matrix = new ArrayList<Position>();
		_searchSequence = new ArrayList<Position>();
		setSearchSequence2();
	}

	private void setSearchSequence2() {
		_searchSequence.add(new Position(0, -1));
		_searchSequence.add(new Position(1, -1));
		_searchSequence.add(new Position(1, 0));
		_searchSequence.add(new Position(1, 1));
		_searchSequence.add(new Position(0, 1));
		_searchSequence.add(new Position(-1, 1));
		_searchSequence.add(new Position(-1, 0));
		_searchSequence.add(new Position(-1, -1));
		
		/*
		_searchSequence.add(new Position(-1, -2));
		_searchSequence.add(new Position(0, -2));
		_searchSequence.add(new Position(1, -2));
		_searchSequence.add(new Position(2, -2));
		_searchSequence.add(new Position(2, -1));
		_searchSequence.add(new Position(2, 0));
		_searchSequence.add(new Position(2, 1));
		_searchSequence.add(new Position(2, 2));
		_searchSequence.add(new Position(1, 2));
		_searchSequence.add(new Position(0, 2));
		_searchSequence.add(new Position(-1, 2));
		_searchSequence.add(new Position(-2, 2));
		_searchSequence.add(new Position(-2, 1));
		_searchSequence.add(new Position(-2, 0));
		_searchSequence.add(new Position(-2, -2));
		*/
	}

	public void clearMatrix() {
		_matrix.clear();
	}

	public void doSomething() {
		clearMatrix();
		Point position = _scanner.getMouse().getPosition();
		int xx = position.x - 90;
		if (xx < 0)
			xx = 0;
		int yy = position.y - 90;
		if (yy < 0)
			yy = 0;
		Rectangle area = new Rectangle(xx, yy, 180, 180);
		LOGGER.info("Looking for Diggy in " + area);
		try {
			Pixel p = _scanner.findDiggy(area);
			if (p != null) {
				Position start = new Position(0, 0, null, State.WALKABLE);
				start._coords = p;
				_matrix.add(start);
				Position pos = start;
				do {
					Position newPos = findGreenRecursive(pos);
					if (newPos != null)
						pos = newPos;
					// TODO check popups, other conditions, etc.
					LOGGER.info("RRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRR");
					_mouse.delay(1000);
					
				} while (true);
			}

		} catch (IOException e) {
			e.printStackTrace();
		} catch (AWTException e) {
			e.printStackTrace();
		} catch (RobotInterruptedException e) {
		}
		LOGGER.info("DONE...");

	}
	
	private void addPos(Position pos) {
		boolean contains = false;
		for (Position position : _matrix) {
	    if (position.same(pos)) {
	    	contains = true;
	    	position._coords = pos._coords;
	    	position._state = pos._state;
	    	position._prev = pos._prev;
	    	break;
	    }
    }
		if (!contains)
		  _matrix.add(pos);
	}

	private Position findGreenRecursive(Position pos) throws IOException, AWTException, RobotInterruptedException {
		if (canMove(pos, 1, 0)) {
			ensureArea(pos, 1, 0);
			Position doCell = doCell(pos, 1, 0);
			if (doCell != null)
				return doCell;
		}

		if (canMove(pos, 0, 1)) {
			ensureArea(pos, 0, 1);
			Position doCell = doCell(pos, 0, 1);
			if (doCell != null)
				return doCell;
		}
		
		if (canMove(pos, 0, -1)) {
			ensureArea(pos, 0, -1);
			Position doCell = doCell(pos, 0, -1);
			if (doCell != null)
				return doCell;
		}
		
		if (canMove(pos, -1, 0)) {
			ensureArea(pos, -1, 0);
			Position doCell = doCell(pos, -1, 0);
			if (doCell != null)
				return doCell;
		}
		
		LOGGER.info("End " + pos);
		return null;
	}

	private void ensureArea(Position pos, int rowOffset, int colOffset) throws RobotInterruptedException, IOException, AWTException {
		int xx = pos._coords.x + rowOffset * 120;
		int yy = pos._coords.y + colOffset * 120;
		Rectangle area = _scanner.getScanArea();
		int xCorrection = 0;
		int yCorrection = 0;
		int step = 120;
		if (rowOffset > 0) {
			//check east
      int eastBorder = area.x + area.width;
      if (xx > eastBorder) {
      	xCorrection = - step; 
      }
		} else if (rowOffset < 0) {
			//check west
      int westBorder = area.x;
      if (xx < westBorder) {
      	xCorrection = step; 
      }
		}
		
		if (colOffset > 0) {
			//check south
			int southBorder = area.y + area.height;
			if (yy > southBorder) {
				yCorrection = - step; 
			}
		} else if (colOffset < 0) {
			//check north
			int northBorder = area.y;
			if (yy < northBorder) {
				yCorrection = step; 
			}
		}
		
		if (xCorrection != 0 || yCorrection != 0) {
			_mouse.drag(pos._coords.x, pos._coords.y, pos._coords.x+xCorrection, pos._coords.y+yCorrection);
			pos._coords.x += xCorrection; 
			pos._coords.y += yCorrection;
			
			Pixel p;
			int tries = 0;
			do {
				_mouse.delay(1500);
				p = lookForDiggyAroundHere(pos._coords, tries % 2 + 1);
				tries++;
			} while (p == null && tries < 3);

			if (p != null) {
				LOGGER.info("Found diggy in attempt " + tries);
				
				int rowCorrective = 0;
				if (pos._coords.x - p.x > 0)
					rowCorrective = -1;
				else if (pos._coords.x - p.x < 0)
					rowCorrective = 1;

				int colCorrective = 0;
				if (pos._coords.y - p.y > 0)
					colCorrective = -1;
				else if (pos._coords.y - p.y < 0)
					colCorrective = 1;
				
				if (rowCorrective != 0 || colCorrective != 0) {
					//need to move it
					
				}
				pos._row += rowCorrective;
				pos._col += colCorrective;
				pos._coords = p;

			} else {
				LOGGER.info("UH OH! I Lost diggy...");
				
			}

			
		}
	  
  }

	private Position doCell(Position pos, int rowOffset, int colOffset) throws AWTException, RobotInterruptedException, IOException {
	  LOGGER.info(rowOffset +", " + colOffset);
	  //CLICK new CELL
	  Position newPos = new Position(pos._row + rowOffset, pos._col + colOffset, pos, State.WALKABLE);
	  newPos._coords = new Pixel(pos._coords.x + rowOffset * 60, pos._coords.y + colOffset * 60);
	  Pixel p = lookForGreenHere(newPos._coords);
	  if (p != null) {
	  	_mouse.click(newPos._coords.x + 30, newPos._coords.y + 30);
	  	_mouse.delay(5000);
	  }
	  
	  //and click again
	  _mouse.click(newPos._coords.x + 30, newPos._coords.y + 30);
	  _mouse.delay(300);
	  int tries = 0;
	  do {
	  	_mouse.delay(250);
	  	tries++;
	  } while (!_scanner.isDiggyExactlyHere(newPos._coords) && tries < 2);

	  //CHECK DID DIGGY MOVED
	  if (_scanner.isDiggyExactlyHere(newPos._coords)) {
	  	//YES. move on
	  	addPos(newPos);
	  	Position newPos2 = findGreenRecursive(newPos);
	  	if (newPos2 != null)
	  		return newPos2;
	  	//_matrix.remove(newPos);
	  	_mouse.click(pos._coords.x + 30, pos._coords.y + 30);
	  	_mouse.delay(1000);

	  } else {
	  	//NO. IT'S AN OBSTACLE
	  	newPos._state = State.OBSTACLE;
	  	addPos(newPos);
	  	tries = 0;
	  	Pixel pd;
	  	do {
	  		_mouse.delay(1000);
	  		tries++;
	  	  pd = lookForDiggyAroundHere(newPos._coords, tries % 2 + 1);
	  	} while (pd == null && tries < 3);
	  	if (pd != null) {
	  		//we found it
	  		//newPos = new Position(pos._row + 1, pos._col + 0, pos, Status.WALKABLE);
	  		//what should i do then?
	  	}
	  	
	  }
	  
	  //_matrix.remove(newPos);
	  
	  return null;
  }

	private Position lookForGreen(Position pos) throws RobotInterruptedException, IOException, AWTException {
		LOGGER.info("looking for greens...");
		for (Position position : _searchSequence) {
			Pixel pp = new Pixel(pos._coords.x + position._row * 60, pos._coords.y + position._col * 60);
			Pixel p = lookForGreenHere(pp);
			if (p != null) {
				Position newPos = new Position(position._row, position._col, pos, State.GREEN);
				newPos._coords = pp;
				LOGGER.info("Found one..." + newPos);
				return newPos;
			} else {
				_mouse.delay(150);
			}
		}
		return null;
	}

	private Position clickTheGreen(Position newPos) throws RobotInterruptedException, IOException, AWTException {
		// click the green
		int tries = 0;
		// do {
		tries++;
		_mouse.click(newPos._coords.x + 30, newPos._coords.y + 30);
		LOGGER.info("click...");
		_mouse.delay(500);
		// } while (lookForGreenHere2(newPos._coords) != null && tries < 5);

		// ////// TODO check is loading -> new matrix
		Pixel p;
		tries = 0;
		do {
			_mouse.delay(1500);
			p = lookForDiggyAroundHere(newPos._coords, tries % 2 + 1);
			tries++;
		} while (p == null && tries < 7);

		if (p != null) {
			LOGGER.info("Found diggy in attempt " + tries);
			
			int rowCorrective = 0;
			if (newPos._coords.x - p.x > 0)
				rowCorrective = -1;
			else if (newPos._coords.x - p.x < 0)
				rowCorrective = 1;

			int colCorrective = 0;
			if (newPos._coords.y - p.y > 0)
				colCorrective = -1;
			else if (newPos._coords.y - p.y < 0)
				colCorrective = 1;
			
			if (rowCorrective != 0 || colCorrective != 0) {
				//need to move it
				
			}
			newPos._row += rowCorrective;
			newPos._col += colCorrective;
			newPos._coords = p;

		} else {
			LOGGER.info("UH OH! I Lost diggy...");
		}
		return newPos;
	}

	private boolean canMove(Position oldPos, int row, int col) {
		Position newPos = new Position(oldPos._row + row, oldPos._col + col, oldPos, State.WALKABLE);
		boolean canMove = true;
		for (Position position : _matrix) {
			if (position.same(newPos)) {
				if (position._state == State.UNKNOWN)
					continue;
				else if (position._state == State.WALKABLE || position._state == State.OBSTACLE) {
					canMove = false;
					break;
				}
			}
		}
		return canMove;
	}

	private Pixel lookForDiggyAroundHere(Pixel pp, int cellRange) throws IOException, RobotInterruptedException, AWTException {
		Rectangle area = new Rectangle(pp.x - cellRange * 60, pp.y - cellRange * 60, cellRange + 120 + 60, cellRange + 120 + 60);
		Pixel res = _scanner.findDiggy(area);
		LOGGER.info("Looking for diggy in " + pp + " " + res);
		return res;
	}

	
	private BufferedImage filterGreen(BufferedImage image) {
		FastBitmap fb1 = new FastBitmap(image);
    ExtractRGBChannel extractChannel = new ExtractRGBChannel(Channel.G);
	  fb1 = extractChannel.Extract(fb1);
		//fb1.saveAsBMP("temp.bmp");
		Threshold thr = new Threshold(170);
		thr.applyInPlace(fb1);
		//colorFiltering.applyInPlace(fb1);
		//fb1.saveAsBMP("temp2.bmp");
		return fb1.toBufferedImage();
	}
	
	private Pixel lookForGreenHere(Pixel pp) throws AWTException, RobotInterruptedException, IOException {
		Rectangle area = new Rectangle(pp.x, pp.y, 60, 60);
		BufferedImage image1 = new Robot().createScreenCapture(area);
		_mouse.mouseMove(pp.x + 30, pp.y + 58);
		_mouse.delay(100);
		BufferedImage image2 = new Robot().createScreenCapture(area);
		List<Blob> blobs = new MotionDetector().detect(image1, image2);
		// FastBitmap fb2 = new FastBitmap(image2);
		// for (Blob blob : blobs) {
		// //fb2.saveAsPNG("BLOB_" + blob.getCenter().y + "_" + blob.getCenter().x + "_" + System.currentTimeMillis()+".png");
		// try {
		// BufferedImage subimage = image2.getSubimage(blob.getBoundingBox().x, blob.getBoundingBox().y, blob.getBoundingBox().width, blob.getBoundingBox().height);
		//
		// _scanner.writeImage(subimage, "BLOB_" + blob.getCenter().y + "_" + blob.getCenter().x + "_" + System.currentTimeMillis()+".png");
		// } catch (Throwable t) {
		// System.out.println(blob);
		// }
		// }
		if (blobs.size() > 0) {
			//we have movement, but let's see is it green
			image2 = filterGreen(image2.getSubimage(0, 0, 15, 15));
			ImageData id = _scanner.getImageData("greenTL.bmp");
			Pixel ppp = _comparator.findImage(id.getImage(), image2, id.getColorToBypass());
			if (ppp != null)
			  return pp;
		}
		return null;
	}

	private void setSearchSequence() {
		_searchSequence.add(new Position(0, -1));
		_searchSequence.add(new Position(0, -2));
		_searchSequence.add(new Position(-1, -1));
		_searchSequence.add(new Position(-1, -2));
		_searchSequence.add(new Position(1, -1));
		_searchSequence.add(new Position(1, -2));
		_searchSequence.add(new Position(-1, 0));
		_searchSequence.add(new Position(-2, 0));
		_searchSequence.add(new Position(1, 0));
		_searchSequence.add(new Position(2, 0));
		_searchSequence.add(new Position(2, -1));
		_searchSequence.add(new Position(2, -2));
		_searchSequence.add(new Position(-2, -1));
		_searchSequence.add(new Position(-2, -2));
	
		_searchSequence.add(new Position(0, 1));
		_searchSequence.add(new Position(0, 2));
		_searchSequence.add(new Position(-1, 1));
		_searchSequence.add(new Position(-1, 2));
		_searchSequence.add(new Position(1, 1));
		_searchSequence.add(new Position(1, 2));
		_searchSequence.add(new Position(2, 1));
		_searchSequence.add(new Position(2, 2));
		_searchSequence.add(new Position(-2, 1));
		_searchSequence.add(new Position(-2, 2));
	}
}
