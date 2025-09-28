package com.minekarta.advancedcorerealms.manager;

import com.minekarta.advancedcorerealms.AdvancedCoreRealms;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class LanguageManager {

    private final AdvancedCoreRealms plugin;
    private final Map<String, FileConfiguration> languageConfigs;
    private String currentLanguage;
    private final MiniMessage miniMessage;

    public LanguageManager(AdvancedCoreRealms plugin) {
        this.plugin = plugin;
        this.languageConfigs = new HashMap<>();
        this.miniMessage = MiniMessage.miniMessage();
    }

    public void loadLanguage() {
        // Create languages directory if it doesn't exist
        File languagesDir = new File(plugin.getDataFolder(), "languages");
        if (!languagesDir.exists()) {
            languagesDir.mkdirs();
        }

        // Load the configured language
        this.currentLanguage = plugin.getConfig().getString("language", "en");

        // Load language files
        loadLanguageFile("en");
        loadLanguageFile("es");
        loadLanguageFile("id");

        // Create default language file if it doesn't exist
        createDefaultLanguageFile();
    }

    private void loadLanguageFile(String langCode) {
        File languageFile = new File(plugin.getDataFolder(), "languages/" + langCode + ".yml");
        if (languageFile.exists()) {
            FileConfiguration config = YamlConfiguration.loadConfiguration(languageFile);
            languageConfigs.put(langCode, config);
        }
    }

    private void createDefaultLanguageFile() {
        String[] languages = {"en", "es", "id"};

        for (String lang : languages) {
            File languageFile = new File(plugin.getDataFolder(), "languages/" + lang + ".yml");

            if (!languageFile.exists()) {
                try {
                    languageFile.createNewFile();

                    // Set default language content based on the language
                    FileConfiguration config = YamlConfiguration.loadConfiguration(languageFile);

                    // English defaults
                    if (lang.equals("en")) {
                        config.set("prefix", "<dark_gray>[<#55FF55>Realms<dark_gray>] <reset>");
                        config.set("command.help", "<green>Showing help for AdvancedCoreRealms...");
                        config.set("command.reloaded", "<green>Configuration reloaded successfully.");
                        config.set("world.created", "<green>Successfully created your new realm named <yellow>%world%<green>.");
                        config.set("world.deleted", "<red>Successfully deleted your realm <yellow>%world%<red>.");
                        config.set("world.teleport", "<gray>Teleporting you to <yellow>%world%<gray>...");
                        config.set("error.no-permission", "<red>You do not have permission to use this command.</red>");
                        config.set("error.no-permission-create", "<red>You do not have permission to create a realm.</red>");
                        config.set("error.world-exists", "<red>A realm with that name already exists.");
                        config.set("error.not-owner", "<red>You are not the owner of this realm.</red>");
                        config.set("realm.delete_command_info", "<red>To delete this realm, use: /realms delete %realm%");
                        config.set("realm.invite_command_info", "<yellow>To invite a player, use: /realms invite %realm% <player>");
                        config.set("realm.player_kicked", "<green>Kicked %player% from the realm.");
                        config.set("error.cannot_kick_player", "<red>You can only kick members from your own realm.");
                        config.set("border.color_set", "<green>Border color set to %color%!");
                        config.set("realm.spawn_set", "<green>Spawn point set for realm %realm%.");
                        config.set("error.must_be_in_realm", "<red>You must be in the realm world to do that.");
                        config.set("realm.player_limit_set", "<green>Player limit for %realm% set to %limit%.");
                        config.set("error.no_realm_to_upgrade", "<red>You do not own a realm to upgrade.");
                        config.set("error.upgrade_max_level", "<red>This upgrade is already at the maximum level.");
                        config.set("error.not_enough_money", "<red>You do not have enough money. Cost: $%cost%");
                        config.set("upgrade.success", "<green>Successfully upgraded %upgrade%!");
                        config.set("error.upgrade_failed", "<red>There was an error purchasing the upgrade.");
                        config.set("error.players_only", "<red>Only players can use this command!");
                        config.set("error.usage.create", "<red>Usage: /realms create <name> [type]");
                        config.set("error.max_realms_reached", "<red>You have reached the maximum number of Realms");
                        config.set("error.invalid_world_type", "<red>Invalid world type! Valid types: FLAT, NORMAL, AMPLIFIED");
                        config.set("realm.creation_started", "<yellow>Creating Realms, Please wait");
                        config.set("error.usage.delete", "<red>Usage: /realms delete <world>");
                        config.set("error.realm_not_found", "<red>Realm does not exist!");
                        config.set("error.usage.teleport", "<red>Usage: /realms tp <world>");
                        config.set("realm.list.header_own", "<gold>=== Your Realms ===");
                        config.set("realm.list.header_invited", "<gold>=== Invited Realms ===");
                        config.set("realm.list.entry", "<aqua>- %name% (%status%)");
                        config.set("error.usage.invite", "<red>Usage: /realms invite <world> <player>");
                        config.set("error.player_not_online", "<red>Player is not online!");
                        config.set("error.usage.transfer", "<red>Usage: /realms transfer <world> <player>");
                        config.set("realm.transfer_success", "<green>Realm ownership transferred successfully!");
                        config.set("error.no_previous_location", "<red>No previous location found!");
                        config.set("realm.teleport_back_success", "<green>Teleported to your previous location.");
                        config.set("error.item_not_transferable", "<red>You cannot take this item outside of the realm!");
                        config.set("realm.invite_sent", "<green>Invitation sent to %player%!");
                        config.set("realm.invite_received", "<gold>%player% has invited you to join their realm '%realm%'!");
                        config.set("realm.invite_instructions", "<yellow>Type <white>/realms accept</white> to join or <white>/realms deny</white> to decline.");
                        config.set("realm.invite_accepted", "<green>You have joined realm '%realm%'!");
                        config.set("realm.invite_denied", "<red>You have declined the invitation to join realm '%realm%'.");
                        config.set("error.no_pending_invites", "<red>You have no pending invitations.");
                    }
                    // Spanish defaults
                    else if (lang.equals("es")) {
                        config.set("prefix", "<dark_gray>[<#55FF55>Reinos<dark_gray>] <reset>");
                        config.set("command.help", "<green>Mostrando ayuda para AdvancedCoreRealms...");
                        config.set("command.reloaded", "<green>Configuración recargada exitosamente.");
                        config.set("world.created", "<green>Se creó exitosamente tu nuevo reino llamado <yellow>%world%<green>.");
                        config.set("world.deleted", "<red>Se eliminó exitosamente tu reino <yellow>%world%<red>.");
                        config.set("world.teleport", "<gray>Teletransportándote a <yellow>%world%<gray>...");
                        config.set("error.no-permission", "<red>No tienes permiso para usar este comando.</red>");
                        config.set("error.no-permission-create", "<red>No tienes permiso para crear un reino.</red>");
                        config.set("error.world-exists", "<red>Ya existe un reino con ese nombre.");
                        config.set("error.not-owner", "<red>No eres el propietario de este reino.</red>");
                        config.set("realm.delete_command_info", "<red>Para eliminar este reino, usa: /realms delete %realm%");
                        config.set("realm.invite_command_info", "<yellow>Para invitar a un jugador, usa: /realms invite %realm% <jugador>");
                        config.set("realm.player_kicked", "<green>Expulsado %player% del reino.");
                        config.set("error.cannot_kick_player", "<red>Solo puedes expulsar miembros de tu propio reino.");
                        config.set("border.color_set", "<green>¡Color del borde establecido a %color%!");
                        config.set("realm.spawn_set", "<green>Punto de aparición establecido para el reino %realm%.");
                        config.set("error.must_be_in_realm", "<red>Debes estar en el mundo del reino para hacer eso.");
                        config.set("realm.player_limit_set", "<green>Límite de jugadores para %realm% establecido a %limit%.");
                        config.set("error.no_realm_to_upgrade", "<red>No posees un reino para mejorar.");
                        config.set("error.upgrade_max_level", "<red>Esta mejora ya está en el nivel máximo.");
                        config.set("error.not_enough_money", "<red>No tienes suficiente dinero. Costo: $%cost%");
                        config.set("upgrade.success", "<green>¡Mejorado %upgrade% exitosamente!");
                        config.set("error.upgrade_failed", "<red>Hubo un error al comprar la mejora.");
                        config.set("error.players_only", "<red>¡Solo los jugadores pueden usar este comando!");
                        config.set("error.usage.create", "<red>Uso: /realms create <nombre> [tipo]");
                        config.set("error.max_realms_reached", "<red>Has alcanzado el número máximo de Reinos");
                        config.set("error.invalid_world_type", "<red>¡Tipo de mundo inválido! Tipos válidos: FLAT, NORMAL, AMPLIFIED");
                        config.set("realm.creation_started", "<yellow>Creando Reinos, por favor espera");
                        config.set("error.usage.delete", "<red>Uso: /realms delete <mundo>");
                        config.set("error.realm_not_found", "<red>¡El reino no existe!");
                        config.set("error.usage.teleport", "<red>Uso: /realms tp <mundo>");
                        config.set("realm.list.header_own", "<gold>=== Tus Reinos ===");
                        config.set("realm.list.header_invited", "<gold>=== Reinos Invitados ===");
                        config.set("realm.list.entry", "<aqua>- %name% (%status%)");
                        config.set("error.usage.invite", "<red>Uso: /realms invite <mundo> <jugador>");
                        config.set("error.player_not_online", "<red>¡El jugador no está en línea!");
                        config.set("error.usage.transfer", "<red>Uso: /realms transfer <mundo> <jugador>");
                        config.set("realm.transfer_success", "<green>¡Propiedad del reino transferida exitosamente!");
                        config.set("error.no_previous_location", "<red>¡No se encontró una ubicación anterior!");
                        config.set("realm.teleport_back_success", "<green>Teletransportado a tu ubicación anterior.");
                        config.set("error.item_not_transferable", "<red>¡No puedes llevar este objeto fuera del reino!");
                        config.set("realm.invite_sent", "<green>¡Invitación enviada a %player%!");
                        config.set("realm.invite_received", "<gold>¡%player% te ha invitado a unirte a su reino '%realm%'!");
                        config.set("realm.invite_instructions", "<yellow>Escribe <white>/realms accept</white> para unirte o <white>/realms deny</white> para rechazar.");
                        config.set("realm.invite_accepted", "<green>¡Te has unido al reino '%realm%'!");
                        config.set("realm.invite_denied", "<red>Has rechazado la invitación para unirte al reino '%realm%'.");
                        config.set("error.no_pending_invites", "<red>No tienes invitaciones pendientes.");
                    }
                    // Indonesian defaults
                    else if (lang.equals("id")) {
                        config.set("prefix", "<dark_gray>[<#55FF55>Realms<dark_gray>] <reset>");
                        config.set("command.help", "<green>Menampilkan bantuan untuk AdvancedCoreRealms...");
                        config.set("command.reloaded", "<green>Konfigurasi berhasil dimuat ulang.");
                        config.set("world.created", "<green>Berhasil membuat realm baru bernama <yellow>%world%<green>.");
                        config.set("world.deleted", "<red>Berhasil menghapus realm <yellow>%world%<red>.");
                        config.set("world.teleport", "<gray>Teleportasi ke <yellow>%world%<gray>...");
                        config.set("error.no-permission", "<red>Anda tidak memiliki izin untuk menggunakan perintah ini.");
                        config.set("error.no-permission-create", "<red>Anda tidak memiliki izin untuk membuat realm.</red>");
                        config.set("error.world-exists", "<red>Realm dengan nama tersebut sudah ada.");
                        config.set("error.not-owner", "<red>Anda bukan pemilik dari realm ini.</red>");
                        config.set("realm.delete_command_info", "<red>Untuk menghapus realm ini, gunakan: /realms delete %realm%");
                        config.set("realm.invite_command_info", "<yellow>Untuk mengundang pemain, gunakan: /realms invite %realm% <pemain>");
                        config.set("realm.player_kicked", "<green>Mengeluarkan %player% dari realm.");
                        config.set("error.cannot_kick_player", "<red>Anda hanya dapat mengeluarkan anggota dari realm Anda sendiri.");
                        config.set("border.color_set", "<green>Warna border diatur ke %color%!");
                        config.set("realm.spawn_set", "<green>Titik spawn diatur untuk realm %realm%.");
                        config.set("error.must_be_in_realm", "<red>Anda harus berada di dunia realm untuk melakukan itu.");
                        config.set("realm.player_limit_set", "<green>Batas pemain untuk %realm% diatur ke %limit%.");
                        config.set("error.no_realm_to_upgrade", "<red>Anda tidak memiliki realm untuk ditingkatkan.");
                        config.set("error.upgrade_max_level", "<red>Peningkatan ini sudah mencapai level maksimum.");
                        config.set("error.not_enough_money", "<red>Uang Anda tidak cukup. Biaya: $%cost%");
                        config.set("upgrade.success", "<green>Berhasil meningkatkan %upgrade%!");
                        config.set("error.upgrade_failed", "<red>Terjadi kesalahan saat membeli peningkatan.");
                        config.set("error.players_only", "<red>Hanya pemain yang dapat menggunakan perintah ini!");
                        config.set("error.usage.create", "<red>Penggunaan: /realms create <nama> [tipe]");
                        config.set("error.max_realms_reached", "<red>Anda telah mencapai jumlah maksimum Realm");
                        config.set("error.invalid_world_type", "<red>Tipe dunia tidak valid! Tipe yang valid: FLAT, NORMAL, AMPLIFIED");
                        config.set("realm.creation_started", "<yellow>Membuat Realm, harap tunggu");
                        config.set("error.usage.delete", "<red>Penggunaan: /realms delete <dunia>");
                        config.set("error.realm_not_found", "<red>Realm tidak ditemukan!");
                        config.set("error.usage.teleport", "<red>Penggunaan: /realms tp <dunia>");
                        config.set("realm.list.header_own", "<gold>=== Realm Anda ===");
                        config.set("realm.list.header_invited", "<gold>=== Realm Undangan ===");
                        config.set("realm.list.entry", "<aqua>- %name% (%status%)");
                        config.set("error.usage.invite", "<red>Penggunaan: /realms invite <dunia> <pemain>");
                        config.set("error.player_not_online", "<red>Pemain tidak online!");
                        config.set("error.usage.transfer", "<red>Penggunaan: /realms transfer <dunia> <pemain>");
                        config.set("realm.transfer_success", "<green>Kepemilikan realm berhasil ditransfer!");
                        config.set("error.no_previous_location", "<red>Tidak ada lokasi sebelumnya yang ditemukan!");
                        config.set("realm.teleport_back_success", "<green>Berhasil teleportasi ke lokasi sebelumnya.");
                        config.set("error.item_not_transferable", "<red>Anda tidak dapat membawa item ini keluar dari realm!");
                        config.set("realm.invite_sent", "<green>Undangan terkirim ke %player%!");
                        config.set("realm.invite_received", "<gold>%player% telah mengundang Anda untuk bergabung dengan realm mereka '%realm%'!");
                        config.set("realm.invite_instructions", "<yellow>Ketik <white>/realms accept</white> untuk bergabung atau <white>/realms deny</white> untuk menolak.");
                        config.set("realm.invite_accepted", "<green>Anda telah bergabung dengan realm '%realm%'!");
                        config.set("realm.invite_denied", "<red>Anda telah menolak undangan untuk bergabung dengan realm '%realm%'.");
                        config.set("error.no_pending_invites", "<red>Anda tidak memiliki undangan yang tertunda.");
                    }

                    config.save(languageFile);
                    languageConfigs.put(lang, config);
                } catch (IOException e) {
                    plugin.getLogger().severe("Could not create language file: " + e.getMessage());
                }
            }
        }
    }

    public String getMessage(String key) {
        FileConfiguration config = languageConfigs.get(currentLanguage);
        if (config == null) {
            // Fallback to English if configured language doesn't exist
            config = languageConfigs.get("en");
        }

        if (config != null) {
            String message = config.getString(key);
            if (message != null) {
                return message; // Return raw message
            }
        }

        // Fallback to English if key doesn't exist in current language
        FileConfiguration enConfig = languageConfigs.get("en");
        if (enConfig != null) {
            return enConfig.getString(key, key); // Return key if not found
        }

        return key; // Return the key if no language files are available
    }

    public String getMessage(String key, String... placeholders) {
        String message = getMessage(key);

        // Replace placeholders in the format %placeholder%
        for (int i = 0; i < placeholders.length; i += 2) {
            if (i + 1 < placeholders.length) {
                message = message.replace(placeholders[i], placeholders[i + 1]);
            }
        }

        return message;
    }

    public Component getMessageAsComponent(String key, String... placeholders) {
        String message = getMessage(key, placeholders);
        return miniMessage.deserialize(message);
    }
    
    public void sendMessage(CommandSender sender, String key, String... placeholders) {
        String prefix = getMessage("prefix");
        String message = getMessage(key, placeholders);

        // Avoid double prefixing if the message key itself is "prefix"
        // Also avoid prefixing empty messages
        if (message == null || message.isEmpty() || key.equals("prefix")) {
            sender.sendMessage(miniMessage.deserialize(message != null ? message : ""));
        } else {
            sender.sendMessage(miniMessage.deserialize(prefix + message));
        }
    }

    public void sendMessage(Player player, String key, String... placeholders) {
        sendMessage((CommandSender) player, key, placeholders);
    }

    public void setCurrentLanguage(String languageCode) {
        this.currentLanguage = languageCode;
    }
}