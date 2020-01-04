package com.wynntils.modules.richpresence.discordgamesdk;

import com.sun.jna.Callback;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;

import java.util.Arrays;
import java.util.List;

/**
 * <i>native declaration : line 403</i><br>
 * This file was autogenerated by
 * <a href="http://jnaerator.googlecode.com/">JNAerator</a>,<br>
 * a tool written by <a href="http://ochafik.com/">Olivier Chafik</a> that
 * <a href="http://code.google.com/p/jnaerator/wiki/CreditsAndLicense">uses a
 * few opensource projects.</a>.<br>
 * For help, please visit
 * <a href="http://nativelibs4java.googlecode.com/">NativeLibs4Java</a> ,
 * <a href="http://rococoa.dev.java.net/">Rococoa</a>, or
 * <a href="http://jna.dev.java.net/">JNA</a>.
 */
public class IDiscordLobbyEvents extends Structure {
    /** C type : on_lobby_update_callback* */
    public IDiscordLobbyEvents.on_lobby_update_callback on_lobby_update;
    /** C type : on_lobby_delete_callback* */
    public IDiscordLobbyEvents.on_lobby_delete_callback on_lobby_delete;
    /** C type : on_member_connect_callback* */
    public IDiscordLobbyEvents.on_member_connect_callback on_member_connect;
    /** C type : on_member_update_callback* */
    public IDiscordLobbyEvents.on_member_update_callback on_member_update;
    /** C type : on_member_disconnect_callback* */
    public IDiscordLobbyEvents.on_member_disconnect_callback on_member_disconnect;
    /** C type : on_lobby_message_callback* */
    public IDiscordLobbyEvents.on_lobby_message_callback on_lobby_message;
    /** C type : on_speaking_callback* */
    public IDiscordLobbyEvents.on_speaking_callback on_speaking;
    /** C type : on_network_message_callback* */
    public IDiscordLobbyEvents.on_network_message_callback on_network_message;

    public interface on_lobby_update_callback extends Callback {
        void apply(Pointer event_data, long lobby_id);
    };

    public interface on_lobby_delete_callback extends Callback {
        void apply(Pointer event_data, long lobby_id, int reason);
    };

    public interface on_member_connect_callback extends Callback {
        void apply(Pointer event_data, long lobby_id, long user_id);
    };

    public interface on_member_update_callback extends Callback {
        void apply(Pointer event_data, long lobby_id, long user_id);
    };

    public interface on_member_disconnect_callback extends Callback {
        void apply(Pointer event_data, long lobby_id, long user_id);
    };

    public interface on_lobby_message_callback extends Callback {
        void apply(Pointer event_data, long lobby_id, long user_id, Pointer data, int data_length);
    };

    public interface on_speaking_callback extends Callback {
        void apply(Pointer event_data, long lobby_id, long user_id, byte speaking);
    };

    public interface on_network_message_callback extends Callback {
        void apply(Pointer event_data, long lobby_id, long user_id, byte channel_id, Pointer data, int data_length);
    };

    public IDiscordLobbyEvents() {
        super();
    }

    protected List<String> getFieldOrder() {
        return Arrays.asList("on_lobby_update", "on_lobby_delete", "on_member_connect", "on_member_update", "on_member_disconnect", "on_lobby_message", "on_speaking", "on_network_message");
    }

    /**
     * @param on_lobby_update      C type : on_lobby_update_callback*<br>
     * @param on_lobby_delete      C type : on_lobby_delete_callback*<br>
     * @param on_member_connect    C type : on_member_connect_callback*<br>
     * @param on_member_update     C type : on_member_update_callback*<br>
     * @param on_member_disconnect C type : on_member_disconnect_callback*<br>
     * @param on_lobby_message     C type : on_lobby_message_callback*<br>
     * @param on_speaking          C type : on_speaking_callback*<br>
     * @param on_network_message   C type : on_network_message_callback*
     */
    public IDiscordLobbyEvents(IDiscordLobbyEvents.on_lobby_update_callback on_lobby_update, IDiscordLobbyEvents.on_lobby_delete_callback on_lobby_delete, IDiscordLobbyEvents.on_member_connect_callback on_member_connect, IDiscordLobbyEvents.on_member_update_callback on_member_update, IDiscordLobbyEvents.on_member_disconnect_callback on_member_disconnect, IDiscordLobbyEvents.on_lobby_message_callback on_lobby_message, IDiscordLobbyEvents.on_speaking_callback on_speaking, IDiscordLobbyEvents.on_network_message_callback on_network_message) {
        super();
        this.on_lobby_update = on_lobby_update;
        this.on_lobby_delete = on_lobby_delete;
        this.on_member_connect = on_member_connect;
        this.on_member_update = on_member_update;
        this.on_member_disconnect = on_member_disconnect;
        this.on_lobby_message = on_lobby_message;
        this.on_speaking = on_speaking;
        this.on_network_message = on_network_message;
    }

    public IDiscordLobbyEvents(Pointer peer) {
        super(peer);
    }

    public static class ByReference extends IDiscordLobbyEvents implements Structure.ByReference {

    };

    public static class ByValue extends IDiscordLobbyEvents implements Structure.ByValue {

    };
}
