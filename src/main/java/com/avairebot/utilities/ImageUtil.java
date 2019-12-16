/*
 * Copyright (c) 2019.
 *
 * This file is part of AvaIre.
 *
 * AvaIre is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AvaIre is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with AvaIre.  If not, see <https://www.gnu.org/licenses/>.
 *
 *
 */
package com.avairebot.utilities;

import java.awt.*;
import java.awt.image.BufferedImage;


public class ImageUtil
{

    /**
     * Joins an image either horizontally or
     * vertically with a bunch of other image.
     *
     * @param img1       the first image
     * @param horizontal if they should be joined horizontally
     * @param imgArray   the img array
     * @return an image containing all the joined pictures.
     */
    public static BufferedImage joinBufferedImage(BufferedImage img1, boolean horizontal ,BufferedImage... imgArray) {

        if(imgArray == null)
        {
            return img1;
        }

        //do some calculate first
        int offset  = 5;

        int width = img1.getWidth();
        int height = img1.getHeight();

        if(horizontal)
        {
            for (BufferedImage image: imgArray)
            {
                width += image.getWidth() + offset;
                height = Math.max(height,image.getHeight()) + offset;
            }
        }
       else
        {
            for (BufferedImage image: imgArray)
            {
                height += image.getHeight() + offset;
                width = Math.max(width,image.getWidth()) + offset;
            }
        }
        //create a new buffer and draw two image into the new image
        BufferedImage newImage = new BufferedImage(width,height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = newImage.createGraphics();

        //draw image
        g2.drawImage(img1, null, 0, 0);

        if(horizontal)
        {
            for (int i = 0; i < imgArray.length; ++i)
            {
                if(i == 0)
                {
                    g2.drawImage(imgArray[i],null,img1.getWidth() + offset,0);
                }
                else
                {
                    g2.drawImage(imgArray[i],null,imgArray[i - 1].getWidth() + offset,0);
                }

            }
        }
       else
        {
            for (int i = 0; i < imgArray.length; ++i)
            {
                if(i == 0)
                {
                    g2.drawImage(imgArray[i],null,0,img1.getHeight() + offset);
                }
                else
                {
                    g2.drawImage(imgArray[i],null,0,imgArray[i - 1].getHeight() + offset);
                }

            }
        }

        g2.dispose();

        return newImage;
    }


    /**
     * Draw an image on top of other image
     * specifying the x, y, and opacity.
     *
     * @param firstImage  the first image
     * @param secondImage  the second image
     * @param opacity the opacity.
     * @param x      the x
     * @param y      the y
     * @return the completed image
     */
    public static BufferedImage drawOnTopOfOtherImage(BufferedImage firstImage, BufferedImage secondImage,
                          float opacity, int x, int y) {
        Graphics2D g2d = firstImage.createGraphics();
        g2d.setComposite(
            AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity / 100));
        g2d.drawImage(secondImage, x, y, null);
        g2d.dispose();
        return firstImage;
    }


}
