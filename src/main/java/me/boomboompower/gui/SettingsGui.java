/*
 *     Copyright (C) 2017 boomboompower
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package me.boomboompower.gui;

import me.dewgs.fpsspoof.FPSSpoofMod;

import me.boomboompower.utils.ChatColor;
import me.boomboompower.gui.utils.ModernButton;
import me.boomboompower.gui.utils.ModernTextBox;

import me.dewgs.fpsspoof.SpoofType;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.Timer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.io.IOException;

public class SettingsGui extends GuiScreen {

    private ModernTextBox textField;
    private ModernTextBox maxField;

    private FPSSpoofMod mod;
    private String input = "";

    // Displaying message on screen
    private Timer timer;
    private String message = "";
    private int messageTimer;

    public SettingsGui(FPSSpoofMod mod) {
        this(mod, "");
    }

    public SettingsGui(FPSSpoofMod mod, String message) {
        this.mod = mod;
        this.input = message;
    }

    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);

        this.textField = new ModernTextBox(0, this.width / 2 - 150, this.height / 2 - 70, 300, 20, "General value", true);
        this.maxField = new ModernTextBox(1, this.width / 2 - 150, this.height / 2 - 46, 300, 20, "Randomizer max value", true);

        this.buttonList.add(new ModernButton(1, this.width / 2 - 75, this.height / 2 + 2, 150, 20, "Randomize FPS"));
        this.buttonList.add(new ModernButton(2, this.width / 2 - 75, this.height / 2 + 26, 150, 20, "Mulitply FPS"));
        this.buttonList.add(new ModernButton(3, this.width / 2 - 75, this.height / 2 + 50, 150, 20, "Add FPS"));
        this.buttonList.add(new ModernButton(4, this.width / 2 - 75, this.height / 2 + 74, 150, 20, "Reset FPS"));

        this.textField.setMaxStringLength(10);
        this.textField.setText(input);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawGuiBackground();

        this.textField.setEnabled(true);
        this.textField.drawTextBox();

        this.maxField.setEnabled(true);
        this.maxField.drawTextBox();

        super.drawScreen(mouseX, mouseY, partialTicks);

        drawTitle("FPSSpoofer v" + FPSSpoofMod.VERSION);

        runMessage();
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        switch (button.id) {
            case 1:
                if (textField.getText().isEmpty() || maxField.getText().isEmpty()) {
                    displayMessage("Inputs cannot be empty!");
                    return;
                }
                try {
                    double value1 = Double.parseDouble(textField.getText());
                    double value2 = Double.parseDouble(maxField.getText());

                    if (value1 > value2) {
                        double newVal = value1;
                        value1 = value2;
                        value2 = newVal;
                    }

                    this.mod.setSpoofType(SpoofType.RANDOMIZER);
                    this.mod.setValues(new double[] { value1, value2 });
                    sendChatMessage("Will spoof FPS ranging from " + value1 + " to " + value2);
                    mc.displayGuiScreen(null);
                } catch (NumberFormatException ex) {
                    displayMessage("Invalid input for general/max value. Only use numbers!");
                }
                break;
            case 2:
                if (textField.getText().isEmpty()) {
                    displayMessage("Max value input cannot be empty!");
                    return;
                }
                try {
                    final double value3 = Double.parseDouble(textField.getText());
                    this.mod.setSpoofType(SpoofType.MULTIPLIER);
                    this.mod.setValues(new double[] { value3 });
                    sendChatMessage("Will spoof FPS based on your real FPS multiplied by " + value3);
                    mc.displayGuiScreen(null);
                } catch (NumberFormatException ex) {
                    displayMessage("Invalid input for max time. Only use numbers!");
                }
                break;
            case 3:
                if (textField.getText().isEmpty()) {
                    displayMessage("Max value input cannot be empty!");
                    return;
                }
                try {
                    final double value3 = Double.parseDouble(textField.getText());
                    this.mod.setSpoofType(SpoofType.ADDITION);
                    this.mod.setValues(new double[] { value3 });
                    sendChatMessage("Will spoof FPS based on " + value3 + " added to your real FPS");
                    mc.displayGuiScreen(null);
                } catch (NumberFormatException ex) {
                    displayMessage("Invalid input for max time. Only use numbers!");
                }
                break;
            case 4:
                this.mod.setSpoofType(SpoofType.OFF);
                this.mod.setValues(null);
                sendChatMessage("Will no longer spoof FPS");
                mc.displayGuiScreen(null);
                break;
        }
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
        mod.saveConfig();
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    protected void keyTyped(char c, int key) {
        if (key == 1) {
            mc.displayGuiScreen(null);
        } else {
            textField.textboxKeyTyped(c, key);
            maxField.textboxKeyTyped(c, key);
        }
    }

    @Override
    protected void mouseClicked(int x, int y, int btn) {
        try {
            super.mouseClicked(x, y, btn);
            textField.mouseClicked(x, y, btn);
            maxField.mouseClicked(x, y, btn);
        } catch (Exception ex) {}
    }

    @Override
    public void sendChatMessage(String msg) {
        Minecraft.getMinecraft().thePlayer.addChatComponentMessage(new ChatComponentText(msg));
    }

    public void display() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        MinecraftForge.EVENT_BUS.unregister(this);
        Minecraft.getMinecraft().displayGuiScreen(this);
    }

    public void drawGuiBackground() {
        long lastPress = System.currentTimeMillis();
        int color = Math.min(255, (int) (2L * (System.currentTimeMillis() - lastPress)));
        Gui.drawRect(0, 0, width, height, 2013265920 + (color << 16) + (color << 8) + color);
    }

    private void drawTitle(String text) {
        drawString(text, this.width / 2, 15, Color.WHITE.getRGB());
        drawHorizontalLine(this.width / 2 - this.mc.fontRendererObj.getStringWidth(text) / 2 - 5, this.width / 2 + this.mc.fontRendererObj.getStringWidth(text) / 2 + 5, 25, Color.WHITE.getRGB());
        drawString("Created by dewgs, Ported by boomboompower", this.width / 2, 30, Color.WHITE.getRGB());
    }

    private void drawString(String message, int x, int y, int color) {
        this.fontRendererObj.drawString(message, (float) (x - this.fontRendererObj.getStringWidth(message) / 2), (float) y, color, false);
    }

    private void displayMessage(String message, Object... replacements) {
        try {
            message = String.format(message, replacements);
        } catch (Exception ex) {
        }

        this.message = ChatColor.translateAlternateColorCodes('&', message);
        this.messageTimer = 80;
    }

    private void runMessage() {
        if (this.messageTimer > 0) {
            --this.messageTimer;
            ScaledResolution resolution = new ScaledResolution(this.mc);
            int scaledWidth = resolution.getScaledWidth();
            int scaledHight = resolution.getScaledHeight();
            float time = (float) this.messageTimer - getTimer().renderPartialTicks;
            int opacity = (int) (time * 255.0F / 20.0F);

            if (opacity > 255) {
                opacity = 255;
            }

            if (opacity > 8) {
                GlStateManager.pushMatrix();
                GlStateManager.translate((float)(scaledWidth / 2), (float) (scaledHight - 80), 0.0F);
                GlStateManager.enableBlend();
                GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
                int color = Color.WHITE.getRGB();

                this.fontRendererObj.drawString(ChatColor.RED + this.message, -this.fontRendererObj.getStringWidth(this.message) / 2, -4, color + (opacity << 24 & -16777216));
                GlStateManager.disableBlend();
                GlStateManager.popMatrix();
            }
        }
    }

    private Timer getTimer() {
        return this.timer == null ? this.timer = ReflectionHelper.getPrivateValue(Minecraft.class, mc, "timer", "field_71428_T") : this.timer;
    }
}
