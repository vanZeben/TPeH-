package com.imdeity.tpeh.commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import ca.xshade.questionmanager.Option;
import ca.xshade.questionmanager.Question;

import com.imdeity.deityapi.Deity;
import com.imdeity.tpeh.TPeH;
import com.imdeity.tpeh.questioner.ConfirmQuestionTask;

public class TeleportAcceptHere implements CommandExecutor {

    private TPeH plugin;

    public TeleportAcceptHere(TPeH instance) {
        this.plugin = instance;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command,
            String commandLabel, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (Deity.perm.has(player, "deity.teleport")) {
                return wrapCommand(player, args);
            } else {
                Deity.chat.sendPlayerError(player, "Teleport", "Insufficient Vespene Gas.");
            }
        }
        return false;
    }

    private boolean wrapCommand(Player player, String[] split) {
        if (split.length == 1) {
            Player teleportee = Deity.server.getOnlinePlayer(split[0]);
            if (teleportee != null && teleportee.isOnline()) {
                if (Deity.perm.isAdmin(teleportee)
                        || Deity.perm.isSubAdmin(teleportee)) {
                    Deity.chat.sendPlayerError(player, "Teleport",
                            "You cannot summon admins.");
                }
                Deity.chat.sendPlayerMessage(teleportee, player.getName()
                        + " has requested that you be teleported to him.");
                this.executeTeleport(player, teleportee);
                return true;
            }
        } else
            Deity.chat.sendPlayerMessage(player, "Teleport",
                    "Sorry but you there was an error in that command.");
        return false;

    }
    
    public void executeTeleport(Player teleporter, Player teleportee) {

        List<Option> options = new ArrayList<Option>();
        options.add(new Option("agree", new ConfirmQuestionTask(teleporter,
                teleportee) {
            @Override
            public void run() {
                Deity.player.teleport(teleportee, teleporter.getLocation());
                Deity.chat.sendPlayerMessage(teleporter, "Teleport", "You teleported "+teleportee.getName()+" to you.");
                Deity.chat.sendPlayerMessage(teleportee, "Teleport", "You teleported to "+teleporter.getName()+".");
                String sql = "INSERT INTO `kingdoms`.`deity_teleports` (`teleporter`, `teleportee`, `is_allowed`) VALUES (?, ?, ?);";
                Deity.data.getDB().Write(sql, teleporter.getName(), teleportee.getName(), 1);
            }
        }));

        options.add(new Option("deny", new ConfirmQuestionTask(teleporter,
                teleportee) {
            @Override
            public void run() {
                Deity.chat.sendPlayerError(teleporter, "Teleport", "Sorry the teleport was denied");
                Deity.chat.sendPlayerMessage(teleportee, "Teleport", "You denied the teleport");
                String sql = "INSERT INTO `kingdoms`.`deity_teleports` (`teleporter`, `teleportee`, `is_allowed`) VALUES (?, ?, ?);";
                Deity.data.getDB().Write(sql, teleporter.getName(), teleportee.getName(), 0);
            }
        }));
        Question question = new Question(teleportee.getName(), "Do you agree to this teleport?", options);
        try {
            plugin.appendQuestion(plugin.getQuestioner(), question);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
