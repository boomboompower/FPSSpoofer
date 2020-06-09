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

import me.boomboompower.gui.SettingsGui;
import me.boomboompower.utils.ChatColor;

import net.minecraft.command.ICommandSender;
import net.minecraft.command.CommandBase;

public class FPSSpoofModCommand extends CommandBase {

    private FPSSpoofMod mod;

    public FPSSpoofModCommand(final FPSSpoofMod mod) {
        this.mod = mod;
    }

    public String getCommandName() {
        return "fpsspoofer";
    }

    public String getCommandUsage(final ICommandSender sender) {
        return ChatColor.RED + "Usage: /" + getCommandName();
    }

    public void processCommand(final ICommandSender sender, final String[] args) {
        if (args.length == 0) {
            new SettingsGui(mod).display();
        } else {
            new SettingsGui(mod, getFrom(args)).display();
        }
    }

    public int getRequiredPermissionLevel() {
        return 0;
    }

    public boolean canCommandSenderUseCommand(final ICommandSender sender) {
        return true;
    }

    // Imagine using Array.join
    public String getFrom(String... args) {
        StringBuilder builder = new StringBuilder();
        for (String arg : args) {
            builder.append(arg).append(" ");
        }
        return builder.toString().trim();
    }
}
