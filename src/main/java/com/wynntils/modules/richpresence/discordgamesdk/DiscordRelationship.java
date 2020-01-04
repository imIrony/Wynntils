package com.wynntils.modules.richpresence.discordgamesdk;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;

import java.util.Arrays;
import java.util.List;

/**
 * <i>native declaration : line 273</i><br>
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
public class DiscordRelationship extends Structure {
    /**
     * @see EDiscordRelationshipType<br>
     *      C type : EDiscordRelationshipType
     */
    public int type;
    /** C type : DiscordUser */
    public DiscordUser user;
    /** C type : DiscordPresence */
    public DiscordPresence presence;

    public DiscordRelationship() {
        super();
    }

    protected List<String> getFieldOrder() {
        return Arrays.asList("type", "user", "presence");
    }

    /**
     * @param type     @see EDiscordRelationshipType<br>
     *                 C type : EDiscordRelationshipType<br>
     * @param user     C type : DiscordUser<br>
     * @param presence C type : DiscordPresence
     */
    public DiscordRelationship(int type, DiscordUser user, DiscordPresence presence) {
        super();
        this.type = type;
        this.user = user;
        this.presence = presence;
    }

    public DiscordRelationship(Pointer peer) {
        super(peer);
    }

    public static class ByReference extends DiscordRelationship implements Structure.ByReference {

    };

    public static class ByValue extends DiscordRelationship implements Structure.ByValue {

    };
}
