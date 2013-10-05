package net.datacrow.util;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ImageIcon;

import net.datacrow.core.IconLibrary;
import net.datacrow.core.resources.DcResources;

public class Rating {
    
    private static final Map<Long, ImageIcon> ratings = new HashMap<Long, ImageIcon>();

    private static final int scale = 10;
    private static final int starSize = 11;

    private static final Color blank = new Color(204, 204, 204);
    private static final Color highest = new Color(255, 255, 0);
    private static final Color high = new Color(255, 204, 0);
    private static final Color mediumHigh = new Color(255, 153, 0);
    private static final Color medium = new Color(255, 102, 0);
    private static final Color low = new Color(255, 0, 0);
    private static final Color terrible = new Color(0, 0, 0);

    public static synchronized ImageIcon getIcon(Long rating) {
        if (ratings.size() == 0) initialize();
        return rating != null ? ratings.get(rating) : null;
    }    
     
    public static synchronized String getLabel(int rating) {
        return rating == -1 ?
               DcResources.getText("lblRatingNotRated") : 
               rating + " " + DcResources.getText("lblRatingOutOf");
    }
    
    private static void initialize() {
        for (int rating = 0; rating <= scale; rating++) {
            BufferedImage bi = new BufferedImage(scale * starSize, 15, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = (Graphics2D) bi.getGraphics();
            
            for (int i = 0; i < rating; i++) {
                g2d.drawImage(IconLibrary._icoRatingOK.getImage(), i*6, 0, null);
            }
            
            for (int i = rating; i < scale; i++) {
                g2d.drawImage(IconLibrary._icoRatingNOK.getImage(), i*6, 0, null);
            }

            bi.flush();
            ratings.put(Long.valueOf(rating), new ImageIcon(bi));
        } 
    }    
}
