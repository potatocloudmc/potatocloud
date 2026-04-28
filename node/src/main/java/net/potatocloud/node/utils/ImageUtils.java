package net.potatocloud.node.utils;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;

public class ImageUtils {
    public static BufferedImage scale(BufferedImage src, int w, int h) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        int x, y;
        int ww = src.getWidth();
        int hh = src.getHeight();
        int[] ys = new int[h];
        for (y = 0; y < h; y++)
            ys[y] = y * hh / h;
        for (x = 0; x < w; x++) {
            int newX = x * ww / w;
            for (y = 0; y < h; y++) {
                int col = src.getRGB(newX, ys[y]);
                img.setRGB(x, y, col);
            }
        }
        return img;
    }

    public static BufferedImage merge(List<BufferedImage> layers) {
        int size[] = {0,0};
        layers.forEach(i -> {
            if(i.getWidth() > size[0]) size[0] = i.getWidth();
            if(i.getWidth() > size[1]) size[1] = i.getWidth();
        });
        BufferedImage merged = new BufferedImage(size[0], size[1], BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = merged.createGraphics();
        g.setComposite(AlphaComposite.SrcOver);
        for (BufferedImage img : layers) {
            if (img != null) g.drawImage(img,
                    0, 0, null);
        }
        g.dispose();
        return merged;
    }
}