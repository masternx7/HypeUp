package dev.mastern.plugins.hypeup.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class SchedulerUtil {
    
    private static final boolean FOLIA;
    
    static {
        boolean folia;
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            folia = true;
        } catch (ClassNotFoundException e) {
            folia = false;
        }
        FOLIA = folia;
    }
    
    public static boolean isFolia() {
        return FOLIA;
    }
    
    public static void runAsync(Plugin plugin, Runnable task) {
        if (FOLIA) {
            Bukkit.getAsyncScheduler().runNow(plugin, scheduledTask -> task.run());
        } else {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, task);
        }
    }
    
    public static void runAsyncDelayed(Plugin plugin, Runnable task, long delay, TimeUnit unit) {
        if (FOLIA) {
            Bukkit.getAsyncScheduler().runDelayed(plugin, scheduledTask -> task.run(), delay, unit);
        } else {
            long ticks = unit.toMillis(delay) / 50;
            Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, task, ticks);
        }
    }
    
    public static void runTask(Plugin plugin, Entity entity, Runnable task) {
        if (FOLIA) {
            entity.getScheduler().run(plugin, scheduledTask -> task.run(), null);
        } else {
            Bukkit.getScheduler().runTask(plugin, task);
        }
    }
    
    public static void runTask(Plugin plugin, Runnable task) {
        if (FOLIA) {
            Bukkit.getGlobalRegionScheduler().run(plugin, scheduledTask -> task.run());
        } else {
            Bukkit.getScheduler().runTask(plugin, task);
        }
    }
    
    public static void runTaskAtLocation(Plugin plugin, Location location, Runnable task) {
        if (FOLIA) {
            Bukkit.getRegionScheduler().run(plugin, location, scheduledTask -> task.run());
        } else {
            Bukkit.getScheduler().runTask(plugin, task);
        }
    }
    
    public static SchedulerTask runAsyncRepeating(Plugin plugin, Runnable task, long delay, long period, TimeUnit unit) {
        if (FOLIA) {
            io.papermc.paper.threadedregions.scheduler.ScheduledTask foliaTask = 
                Bukkit.getAsyncScheduler().runAtFixedRate(plugin, scheduledTask -> task.run(), delay, period, unit);
            return foliaTask::cancel;
        } else {
            long delayTicks = unit.toMillis(delay) / 50;
            long periodTicks = unit.toMillis(period) / 50;
            int taskId = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, task, delayTicks, periodTicks).getTaskId();
            return () -> Bukkit.getScheduler().cancelTask(taskId);
        }
    }
    
    public static void runCommand(Plugin plugin, Runnable command) {
        if (FOLIA) {
            Bukkit.getGlobalRegionScheduler().run(plugin, scheduledTask -> command.run());
        } else {
            Bukkit.getScheduler().runTask(plugin, command);
        }
    }
    
    public static void getEntityLocation(Plugin plugin, Entity entity, Consumer<Location> callback) {
        if (FOLIA) {
            entity.getScheduler().run(plugin, scheduledTask -> {
                callback.accept(entity.getLocation());
            }, null);
        } else {
            callback.accept(entity.getLocation());
        }
    }
    
    public interface SchedulerTask {
        void cancel();
    }
}
