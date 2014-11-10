package com.graphics;

import ar.com.hjg.pngj.ImageInfo;
import ar.com.hjg.pngj.ImageLineInt;
import ar.com.hjg.pngj.PngReader;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

public class Trimmer {

    public static void main(String[] args) {
	System.out.println("FYI:\tLength of args array is " + args.length);
        if (args.length < 1) {
            System.out.println("You have to specify the patch to a .png file or a directory containing .png files, e.g. \"java com.graphics.Trimmer /absolute/path/to/directory\"");
            return;
        }

	String path = args[0];
        File dir = new File(path);
        if (!dir.isDirectory()) {
            trim(dir);
            return;
        }
        File[] files = dir.listFiles();
        String trimmedDirPath = path + "/trimmed";
        File trimmedDir = new File(trimmedDirPath);
        if (!trimmedDir.exists() && !trimmedDir.mkdir()) return;
        if (files == null) return;
        for (File file : files) trim(file);
    }

    public static void trim(File file) {
        if (!file.isFile()) System.out.println("Cannot trim non-png resource " + file.getAbsolutePath());
        PngReader reader = null;
        try {
            reader = new PngReader(file);
            ImageInfo info = reader.getImgInfo();
            if (!info.alpha) throw new NoAlphaChannelException();
            /*
                there're 4 types of channel arrangements: RGB, RGBA, GA(gray scale with alpha), G(gray scale)
                this method works only for type RGBA of variable bit-depth
             */
            int baseMask = (1 << info.bitDepth) - 1;

            int left = info.cols - 1, top = info.rows - 1, right = 0, bottom = 0;
            // ImageLineInt: Represents an image line, integer format (one integer by sample/channel)
            int row = 0;
            while (reader.hasMoreRows()) {
                ImageLineInt lineData = (ImageLineInt) reader.readRow();
                for (int col = 0; col < info.cols; ++col) {
                    int alpha = lineData.getElem(col * info.channels);
                    if (alpha == baseMask) continue;
                    if (col < left) left = col;
                    if (col > right) right = col;
                    if (row < top) top = row;
                    if (row > bottom) bottom = row;
                }
                ++row;
            }
            int w = (right - left + 1), h = (bottom - top + 1);
            Image origin = ImageIO.read(file);
            BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            bi.getGraphics().drawImage(origin, 0, 0, w, h, left, top, left + w, top + h, null);
            String pathPrefix = file.getParent() + "/trimmed/";
            String newName = file.getName();
            File trimmedImage = new File(pathPrefix + newName);
            ImageIO.write(bi, "png", trimmedImage);
        } catch (Exception e) {

        } finally {
            if (reader != null) reader.close();
        }
    }
}
