/*
 *  * Copyright © Wynntils - 2018 - 2020.
 */

package com.wynntils.core.utils.reference;

public class EmeraldSymbols {

    public static final char E = '\u00B2';
    public static final char B = '\u00BD';
    public static final char L = '\u00BC';

    public static final String E_STRING = Character.toString(E).intern();
    public static final String B_STRING = Character.toString(B).intern();
    public static final String L_STRING = Character.toString(L).intern();

    public static final String EMERALDS = (E_STRING).intern();
    public static final String BLOCKS = (E_STRING + B_STRING).intern();
    public static final String LE = (L_STRING + E_STRING).intern();

}
