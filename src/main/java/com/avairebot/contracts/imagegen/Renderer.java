/*
 * Copyright (c) 2018.
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

package com.avairebot.contracts.imagegen;

import com.avairebot.exceptions.RenderNotReadyYetException;

import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public abstract class Renderer {

    public abstract boolean canRender();

    @Nullable
    protected abstract BufferedImage handleRender() throws IOException;

    @Nullable
    public BufferedImage render() throws IOException {
        if (!canRender()) {
            throw new RenderNotReadyYetException("One or more required arguments for the renderer have not been setup yet.");
        }

        return handleRender();
    }

    public byte[] renderToBytes() throws IOException {
        if (!canRender()) {
            throw new RenderNotReadyYetException("One or more required arguments for the renderer have not been setup yet.");
        }

        final BufferedImage bufferedImage = handleRender();
        if (bufferedImage == null) {
            return null;
        }

        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();

        ImageIO.write(bufferedImage, "png", byteStream);
        byteStream.flush();

        byte[] bytes = byteStream.toByteArray();
        byteStream.close();

        return bytes;
    }

    protected final BufferedImage resize(BufferedImage image, int height, int width) {
        Image scaledInstance = image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = resized.createGraphics();

        g2d.drawImage(scaledInstance, 0, 0, null);
        g2d.dispose();

        return resized;
    }

    protected final Color getColor(float red, float green, float blue) {
        return new Color(red / 255F, green / 255F, blue / 255F, 1F);
    }

    protected final Color getColor(float red, float green, float blue, float alpha) {
        return new Color(red / 255F, green / 255F, blue / 255F, alpha / 100F);
    }
}
