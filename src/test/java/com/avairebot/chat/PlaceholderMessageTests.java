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

package com.avairebot.chat;

import com.avairebot.BaseTest;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.MessageEmbed;
import org.junit.Test;

import java.awt.*;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PlaceholderMessageTests extends BaseTest {

    @Test
    public void testDescriptionIsNotChangedIfHasNoPlaceholdersAndIsLessThanTheMaxLength() {
        assertEquals("Lorem ipsum", createWith("Lorem ipsum").toString());

        PlaceholderMessage messageThroughMethod = new PlaceholderMessage(null, null);
        messageThroughMethod.setDescription("Lorem ipsum");

        assertEquals("Lorem ipsum", messageThroughMethod.toString());
    }

    @Test
    public void testDescriptionReplacesPlaceholdersIfOnesIsSet() {
        PlaceholderMessage message = createWith("Hello, :name!");
        message.set("name", "World");

        assertEquals("Hello, World!", message.toString());
    }

    @Test
    public void testDescriptionCanHaveMultiplePlaceholders() {
        PlaceholderMessage message = createWith(":test :placeholders :things");
        message.set("test", "How");
        message.set("placeholders", "are");
        message.set("things", "you?");

        assertEquals("How are you?", message.toString());
    }

    @Test
    public void testDescriptionWithSamePlaceholderMultipleTimesIsAllReplaced() {
        PlaceholderMessage message = createWith(":name :name :name");
        message.set("name", "Test");

        assertEquals("Test Test Test", message.toString());
    }

    @Test
    public void testTitleIsSetWithoutUrlCorrectly() {
        PlaceholderMessage message = createWith(null);
        message.setTitle("Some weird Title");

        MessageEmbed embed = message.buildEmbed();

        assertEquals("Some weird Title", embed.getTitle());
        assertEquals(null, embed.getUrl());
    }

    @Test
    public void testTitleIsSetWithUrlCorrectly() {
        PlaceholderMessage message = createWith(null);
        message.setTitle("Some weird Title", "https://avairebot.com/");

        MessageEmbed embed = message.buildEmbed();

        assertEquals("Some weird Title", embed.getTitle());
        assertEquals("https://avairebot.com/", embed.getUrl());
    }

    @Test
    public void testFooterIsSetWithoutIconCorrectly() {
        PlaceholderMessage message = createWith(null);
        message.setFooter("Some weird title!");

        MessageEmbed embed = message.buildEmbed();

        assertEquals("Some weird title!", embed.getFooter().getText());
        assertEquals(null, embed.getFooter().getIconUrl());
    }

    @Test
    public void testFooterIsSetWithIconCorrectly() {
        PlaceholderMessage message = createWith(null);
        message.setFooter("Some weird title!", "https://i.imgur.com/xW53ysv.gif");

        MessageEmbed embed = message.buildEmbed();

        assertEquals("Some weird title!", embed.getFooter().getText());
        assertEquals("https://i.imgur.com/xW53ysv.gif", embed.getFooter().getIconUrl());
    }

    @Test
    public void testThumbnailIsSetCorrectly() {
        PlaceholderMessage message = createWith(null);
        message.setThumbnail("https://i.imgur.com/wAD0tqK.gif");

        MessageEmbed embed = message.buildEmbed();

        assertEquals("https://i.imgur.com/wAD0tqK.gif", embed.getThumbnail().getUrl());
    }

    @Test
    public void testImageIsSetCorrectly() {
        PlaceholderMessage message = createWith(null);
        message.setImage("https://i.imgur.com/W7qSt0G.gif");

        MessageEmbed embed = message.buildEmbed();

        assertEquals("https://i.imgur.com/W7qSt0G.gif", embed.getImage().getUrl());
    }

    @Test
    public void testTimestampIsSetCorrectly() {
        Instant now = Instant.now();

        PlaceholderMessage message = createWith(null);
        message.setTimestamp(now);

        MessageEmbed embed = message.buildEmbed();

        assertEquals(now.toEpochMilli(), embed.getTimestamp().toInstant().toEpochMilli());
    }

    @Test
    public void testAuthorIsSetWithoutUrlAndIconCorrectly() {
        PlaceholderMessage message = createWith(null);
        message.setAuthor("Senither");

        MessageEmbed embed = message.buildEmbed();

        assertEquals("Senither", embed.getAuthor().getName());
        assertEquals(null, embed.getAuthor().getIconUrl());
        assertEquals(null, embed.getAuthor().getUrl());
    }

    @Test
    public void testAuthorIsSetWithoutIconCorrectly() {
        PlaceholderMessage message = createWith(null);
        message.setAuthor("Senither", "https://avairebot.com/");

        MessageEmbed embed = message.buildEmbed();

        assertEquals("Senither", embed.getAuthor().getName());
        assertEquals("https://avairebot.com/", embed.getAuthor().getUrl());
        assertEquals(null, embed.getAuthor().getIconUrl());
    }

    @Test
    public void testAuthorIsSetCorrectly() {
        PlaceholderMessage message = createWith(null);
        message.setAuthor("Senither", "https://avairebot.com/", "https://i.imgur.com/odFyo1Q.gif");

        MessageEmbed embed = message.buildEmbed();

        assertEquals("Senither", embed.getAuthor().getName());
        assertEquals("https://avairebot.com/", embed.getAuthor().getUrl());
        assertEquals("https://i.imgur.com/odFyo1Q.gif", embed.getAuthor().getIconUrl());
    }

    @Test
    public void testColorIsSetCorrectly() {
        Color color = Color.decode("#BB1150");

        // Can't build an empty embed message, so have to set a description since setting the color alone doesn't count.
        PlaceholderMessage message = createWith("Lorem ipsum");
        message.setColor(color);

        MessageEmbed embed = message.buildEmbed();

        assertEquals(color, embed.getColor());
    }

    @Test
    public void testFieldsAreCreatedCorrectly() {
        PlaceholderMessage message = createWith(null);
        message.addField("Name", "Some Value", false);

        MessageEmbed embed = message.buildEmbed();

        assertEquals(1, embed.getFields().size());
        assertEquals("Name", embed.getFields().get(0).getName());
        assertEquals("Some Value", embed.getFields().get(0).getValue());
        assertEquals(false, embed.getFields().get(0).isInline());
    }

    @Test
    public void testMultipleFieldsAreCreatedCorrectly() {
        PlaceholderMessage message = createWith(null);
        message.addField("Stuff 1", "Some Value", false);
        message.addField("Stuff 2", "Value Some", true);

        MessageEmbed embed = message.buildEmbed();

        assertEquals(2, embed.getFields().size());
        assertEquals("Stuff 1", embed.getFields().get(0).getName());
        assertEquals("Some Value", embed.getFields().get(0).getValue());
        assertEquals(false, embed.getFields().get(0).isInline());
        assertEquals("Stuff 2", embed.getFields().get(1).getName());
        assertEquals("Value Some", embed.getFields().get(1).getValue());
        assertEquals(true, embed.getFields().get(1).isInline());
    }

    private PlaceholderMessage createWith(String description) {
        return new PlaceholderMessage(new EmbedBuilder(), description);
    }
}
