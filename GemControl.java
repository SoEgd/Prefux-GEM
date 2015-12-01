package prefux.controls;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import javafx.animation.PauseTransition;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseDragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TouchEvent;
import javafx.scene.input.TouchPoint;
import javafx.scene.input.ZoomEvent;
import javafx.util.Duration;

import prefux.Display;
import prefux.FxDisplay;
import prefux.data.Edge;
import prefux.data.Node;
import prefux.data.util.Point2D;
import prefux.util.ColorLib;
import prefux.util.PrefuseLib;
import prefux.visual.EdgeItem;
import prefux.visual.NodeItem;
import prefux.visual.VisualItem;

public class GemControl extends ControlAdapter {
	
	private static int NORMAL_COLOR		= ColorLib.rgb(72, 61, 139);	// DARK SLATE BLUE
	private static int SELECTED_COLOR	= ColorLib.rgb(0, 191, 255);	// BLUE
	private static int COLLAPSED_COLOR	= ColorLib.rgb(255, 165, 0);	// ORANGE
    
    private class ItemInfo {
    	private double size;
    	private double[] coordinates = new double[2];
    	
    	ItemInfo(double size, double[] coordinates) {
    		this.size = size;
    		this.coordinates = coordinates;
    	}
    }
    
    HashMap<VisualItem, ItemInfo> map = new HashMap<>();
    private VisualItem currentItem;
    
    private FxDisplay display;
    private double zoomValue = 1;
    private Point2D anchor;
    
    long startTime = 0;
    VisualItem selectedItem;
    
    private Delta delta = new Delta();
    
    /**
     * Create a new collapse control.
     */
    public GemControl() {}
    
    public GemControl(FxDisplay display) {
    	this.display = display;
    }
    
	@Override
	public void itemEvent(VisualItem item, Event e)
	{
		if(item.isFixed()) { // If the algorithm has finished.
			
			
			
			if(e.getEventType() == TouchEvent.TOUCH_PRESSED && item instanceof NodeItem) {
				startTime = System.nanoTime();
			}
			
			else if(e.getEventType() == TouchEvent.TOUCH_STATIONARY && item instanceof NodeItem) {
				long elapsedTime = (System.nanoTime() - startTime) / 1000000; // Milliseconds
				if(selectedItem != item && elapsedTime > 500) {
					if(!item.isHighlighted()) {
						if(selectedItem != null) { // If a node is already selected: deselect it.
							selectedItem.setHighlighted(false);
							selectedItem.setFillColor(NORMAL_COLOR);
						}
						selectedItem = item;
						item.setHighlighted(true);
						item.setFillColor(item.isExpanded() ? SELECTED_COLOR : COLLAPSED_COLOR);
						
						System.out.println("SELECTED: " + selectedItem);
					}
				}
			}
			
			else if(e.getEventType() == TouchEvent.TOUCH_MOVED && item instanceof NodeItem) {
				if(selectedItem == item && item.isExpanded()) {
					TouchEvent ev = (TouchEvent)e;
					TouchPoint point = ev.getTouchPoint();
					
					item.setX(point.getX());
					item.setY(point.getY());
				}
			}
			
			else if(e.getEventType() == TouchEvent.TOUCH_RELEASED && item instanceof NodeItem) {
				long elapsedTime = (System.nanoTime() - startTime) / 1000000; // Milliseconds
				if(elapsedTime < 500) {
					
					if(item.isExpanded()) { // Collapse
						
						// item.setVisible(); does not work, would have been a lot more simple if it did.
						
						currentItem = item;
						if(!map.containsKey(item)) {
							double[] coordinates = new double[2];
							coordinates[0] = item.getX();
							coordinates[1] = item.getY();
							ItemInfo itemInfo = new ItemInfo(item.getSize(), coordinates);
							
							map.put(item, itemInfo);
						}
						
						Node n = (Node)item;
						hideChildren(n);
						
						item.setFillColor(COLLAPSED_COLOR);
						item.setExpanded(false);
						
					} else { // Expand
						
						Node n = (Node)item;
			    		showChildren(n);
			    		
			    		item.setFillColor(item == selectedItem ? SELECTED_COLOR : NORMAL_COLOR);
						item.setExpanded(true);
					}
				}
			}
		}
		
		e.consume();
    }
    
    private void hideChildren(Node n) {
    	Iterator<? extends Node> it = n.outNeighbors();
		while(it.hasNext()) {
			
			Node child = it.next();
			hideChildren(child);
			
			VisualItem vi = (VisualItem)child;
			
			if(!map.containsKey(vi)) {
				double[] coordinates = new double[2];
				coordinates[0] = vi.getX();
				coordinates[1] = vi.getY();
				ItemInfo itemInfo = new ItemInfo(vi.getSize(), coordinates);
				
				map.put(vi, itemInfo);
			}
			
			vi.setSize(0);
			vi.setX(currentItem.getX());
			vi.setY(currentItem.getY());
			
			vi.setExpanded(false);
			vi.setVisible(false);
			
		}
    }
    
    private void showChildren(Node n) {
    	Iterator<? extends Node> it = n.outNeighbors();
		while(it.hasNext()) {
			
			Node child = it.next();
			showChildren(child);
			
			VisualItem vi = (VisualItem)child;
			ItemInfo itemInfo = map.get(vi);
			
			double size = itemInfo.size;
			double[] coordinates = itemInfo.coordinates;
			
			vi.setFillColor(vi == selectedItem ? SELECTED_COLOR : NORMAL_COLOR);
			vi.setX(coordinates[0]);
			vi.setY(coordinates[1]);
			vi.setSize(size);
			
			vi.setExpanded(true);
			vi.setVisible(true);
		}
    }
}
