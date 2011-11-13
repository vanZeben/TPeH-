package com.imdeity.tpeh.questioner;

import org.bukkit.entity.Player;

public class ConfirmQuestionTask extends TPeHQuestionTask {

    protected Player teleporter;
    protected Player teleportee;
    protected String phrase = "Do you agree to this teleport?";


    public ConfirmQuestionTask(Player teleporter, Player teleportee) {
        this.teleporter = teleporter;
        this.teleportee = teleportee; 
    }
    
    @Override
    public void run() {
    }

}
