package com.keuin.rdiffbackup.event.handler;

import com.keuin.rdiffbackup.autobackup.PlayerActivityTracker;
import com.keuin.rdiffbackup.event.OnPlayerConnect;
import com.keuin.rdiffbackup.notification.DistinctNotifiable;
import com.keuin.rdiffbackup.notification.NotificationManager;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.network.ServerPlayerEntity;

public class ConcretePlayerConnectEventHandler implements OnPlayerConnect.PlayerConnectEventCallback {
    private final PlayerActivityTracker playerActivityTracker;

    public ConcretePlayerConnectEventHandler(PlayerActivityTracker playerActivityTracker) {
        this.playerActivityTracker = playerActivityTracker;
    }

    @Override
    public void onPlayerConnect(ClientConnection connection, ServerPlayerEntity player) {
        playerActivityTracker.setCheckpoint();
        NotificationManager.INSTANCE.notifyPlayer(DistinctNotifiable.fromServerPlayerEntity(player));
    }
}
