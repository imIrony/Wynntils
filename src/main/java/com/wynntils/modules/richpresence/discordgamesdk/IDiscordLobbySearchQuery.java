package com.wynntils.modules.richpresence.discordgamesdk;

import com.sun.jna.Callback;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;

import java.util.Arrays;
import java.util.List;

/**
 * <i>native declaration : line 338</i><br>
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
public class IDiscordLobbySearchQuery extends Structure {
    /** C type : filter_callback* */
    public IDiscordRelationshipManager.filter_callback filter;
    /** C type : sort_callback* */
    public IDiscordLobbySearchQuery.sort_callback sort;
    /** C type : limit_callback* */
    public IDiscordLobbySearchQuery.limit_callback limit;
    /** C type : distance_callback* */
    public IDiscordLobbySearchQuery.distance_callback distance;

    public interface filter_callback extends Callback {
        int apply(IDiscordLobbySearchQuery lobby_search_query, Pointer key, int comparison, int cast, Pointer value);
    };

    public interface sort_callback extends Callback {
        int apply(IDiscordLobbySearchQuery lobby_search_query, Pointer key, int cast, Pointer value);
    };

    public interface limit_callback extends Callback {
        int apply(IDiscordLobbySearchQuery lobby_search_query, int limit);
    };

    public interface distance_callback extends Callback {
        int apply(IDiscordLobbySearchQuery lobby_search_query, int distance);
    };

    public IDiscordLobbySearchQuery() {
        super();
    }

    protected List<String> getFieldOrder() {
        return Arrays.asList("filter", "sort", "limit", "distance");
    }

    /**
     * @param filter   C type : filter_callback*<br>
     * @param sort     C type : sort_callback*<br>
     * @param limit    C type : limit_callback*<br>
     * @param distance C type : distance_callback*
     */
    public IDiscordLobbySearchQuery(IDiscordRelationshipManager.filter_callback filter, IDiscordLobbySearchQuery.sort_callback sort, IDiscordLobbySearchQuery.limit_callback limit, IDiscordLobbySearchQuery.distance_callback distance) {
        super();
        this.filter = filter;
        this.sort = sort;
        this.limit = limit;
        this.distance = distance;
    }

    public IDiscordLobbySearchQuery(Pointer peer) {
        super(peer);
    }

    public static class ByReference extends IDiscordLobbySearchQuery implements Structure.ByReference {

    };

    public static class ByValue extends IDiscordLobbySearchQuery implements Structure.ByValue {

    };
}
