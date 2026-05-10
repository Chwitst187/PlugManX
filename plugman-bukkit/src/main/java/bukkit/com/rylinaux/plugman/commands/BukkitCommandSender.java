package bukkit.com.rylinaux.plugman.commands;

import bukkit.com.rylinaux.plugman.PlugManBukkit;
import core.com.rylinaux.plugman.commands.CommandSender;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;


public record BukkitCommandSender(org.bukkit.command.CommandSender bukkitSender) implements CommandSender {
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    @Override
    public void sendMessage(String message) {
        sendMessage(true, message, new Object[0]);
    }

    @Override
    public void sendMessage(boolean prefix, String message) {
        sendMessage(prefix, message, new Object[0]);
    }

    @Override
    public void sendMessage(String message, Object... args) {
        sendMessage(true, message, args);
    }

    @Override
    public void sendMessage(boolean prefix, String message, Object... args) {
        message = PlugManBukkit.getInstance().getMessageFormatter().formatMessage(prefix, message, args);
        bukkitSender.sendMessage(MINI_MESSAGE.deserialize(String.format(message, args)));
    }

    @Override
    public boolean hasPermission(String permission) {
        return bukkitSender.hasPermission(permission);
    }

    @Override
    public boolean isConsole() {
        return !(bukkitSender instanceof Player);
    }

    @SuppressWarnings("unchecked")
    @Override
    public org.bukkit.command.CommandSender getHandle() {
        return bukkitSender();
    }
}
