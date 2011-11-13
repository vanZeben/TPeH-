package com.imdeity.tpeh.questioner;

import com.imdeity.tpeh.TPeH;

import ca.xshade.bukkit.questioner.BukkitQuestionTask;

public abstract class TPeHQuestionTask extends BukkitQuestionTask {
    protected TPeH plugin;

    public void setTPeH(TPeH instance) {
        this.plugin = instance;
    }

    @Override
    public abstract void run();
}
