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

package me.dewgs.fpsspoof;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.client.settings.GameSettings;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Random;

@Mod(modid = FPSSpoofMod.MODID, version = FPSSpoofMod.VERSION, acceptedMinecraftVersions = "*")
public class FPSSpoofMod {

    public static final String MODID = "fpsspoofmod";
    public static final String VERSION = "1.0";

    private File configFile;

    private Random random;
    private long debugUpdateTime;
    private int randomizedFPS;
    private SpoofType spoofType;
    private double[] values;

    public FPSSpoofMod() {
        this.random = new Random();
        this.debugUpdateTime = 0L;
        this.spoofType = SpoofType.OFF;
    }

    public void setSpoofType(final SpoofType spoofType) {
        this.spoofType = spoofType;
    }

    public void setValues(final double[] values) {
        this.values = values;
    }

    public void saveConfig() {
        try {
            final File file = configFile;
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            final FileWriter writer = new FileWriter(file, false);
            StringBuilder values = new StringBuilder();
            if (this.values != null) {
                for (final double value : this.values) {
                    values.append(value).append(" ");
                }
            }
            writer.write(this.spoofType.name() + " " + values);
            writer.close();
        }
        catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private void loadConfig() {
        try {
            final File file = configFile;
            if (!file.exists()) {
                return;
            }
            final BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = reader.readLine()) != null) {
                final String[] args = line.split(" ");
                this.spoofType = SpoofType.valueOf(args[0]);
                this.values = new double[args.length - 1];
                for (int i = 1; i < args.length; ++i) {
                    final double value = Double.parseDouble(args[i]);
                    this.values[i - 1] = value;
                }
            }
            reader.close();
        }
        catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private void resetDebugUpdateTime() {
        try {
            ReflectionHelper.setPrivateValue(Minecraft.class, Minecraft.getMinecraft(), System.currentTimeMillis() + 10000L, "debugUpdateTime", "field_71419_L");
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private int getFPS() {
        try {
            return ReflectionHelper.getPrivateValue(Minecraft.class, Minecraft.getMinecraft(), "fpsCounter", "field_71420_M");
        } catch (Throwable e) {
            e.printStackTrace();
            return 0;
        }
    }

    private void setFPS(final int fpsCounter) {
        try {
            ReflectionHelper.setPrivateValue(Minecraft.class, Minecraft.getMinecraft(), fpsCounter, "fpsCounter", "field_71420_M");
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public SpoofType getSpoofType() {
        return this.spoofType;
    }

    public double[] getValues() {
        return this.values;
    }

    @Mod.EventHandler
    public void preInit(final FMLPreInitializationEvent event) {
        this.configFile = event.getSuggestedConfigurationFile();
    }

    @Mod.EventHandler
    public void init(final FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
        ClientCommandHandler.instance.registerCommand(new FPSSpoofModCommand(this));
        this.loadConfig();
        System.out.println("Successfully initialized FPS Spoofer 1.0 by dewgs");
        System.out.println("Ported to 1.8.9 by boomboompower!");
    }

    @SubscribeEvent
    public void clientTick(final TickEvent.ClientTickEvent event) {
        if (!event.phase.equals(TickEvent.Phase.END)) {
            return;
        }
        this.resetDebugUpdateTime();
        if (System.currentTimeMillis() > this.debugUpdateTime) {
            int displayFPS = 0;
            switch (this.getSpoofType()) {
                case RANDOMIZER: {
                    displayFPS = (int)(this.values[0] + this.random.nextDouble() * (this.values[1] - this.values[0]));
                    break;
                }
                case MULTIPLIER: {
                    displayFPS = this.getFPS() * (int) this.values[0];
                    break;
                }
                case ADDITION: {
                    displayFPS = this.getFPS() + (int) this.values[0];
                    break;
                }
                case OFF: {
                    displayFPS = this.getFPS();
                    break;
                }
            }
            Minecraft.getMinecraft().debug = String.format("%d fps (%d chunk update%s) T: %s%s%s%s%s",
                    displayFPS,
                    RenderChunk.renderChunksUpdated,
                    RenderChunk.renderChunksUpdated != 1 ? "s" : "",
                    (float) Minecraft.getMinecraft().gameSettings.limitFramerate == GameSettings.Options.FRAMERATE_LIMIT.getValueMax() ? "inf" : Integer.valueOf(Minecraft.getMinecraft().gameSettings.limitFramerate),
                    Minecraft.getMinecraft().gameSettings.enableVsync ? " vsync" : "", Minecraft.getMinecraft().gameSettings.fancyGraphics ? "" : " fast",
                    Minecraft.getMinecraft().gameSettings.clouds == 0 ? "" : (Minecraft.getMinecraft().gameSettings.clouds == 1 ? " fast-clouds" : " fancy-clouds"), OpenGlHelper.useVbo() ? " vbo" : ""
            );

            this.setFPS(0);
            this.debugUpdateTime = System.currentTimeMillis() + 1000L;
        }
    }
}