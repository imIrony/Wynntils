/*
 *  * Copyright © Wynntils - 2020.
 */

package com.wynntils.webapi.services;

import com.wynntils.core.utils.Utils;
import com.wynntils.modules.utilities.configs.TranslationConfig;
import net.minecraft.util.text.TextFormatting;

import java.lang.reflect.Constructor;
import java.util.function.Consumer;

public class TranslationManager {

    public static final String TRANSLATED_PREFIX = TextFormatting.GRAY + "➢" + TextFormatting.RESET;

    private static TranslationService translator = null;

    /**
     * Get a TranslationService.
     *
     * @param service An enum describing which translation service is requested.
     * @return An instance of the selected translation service, or null on failure
     */
    public static TranslationService getService(TranslationServices service) {
        try {
            Constructor<? extends TranslationService> ctor = service.serviceClass.getConstructor();
            return ctor.newInstance();
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Get the default TranslationService for the language specified by the user in the settings.
     *
     * @return An instance of the selected translation service, or null on failure
     */
    public static TranslationService getTranslator() {
        // These might not have been created yet, or reset by config changing
        if (TranslationManager.translator == null) {
            TranslationManager.translator = TranslationManager.getService(TranslationConfig.INSTANCE.translationService);
        }
        return translator;
    }

    /**
     * Reset the default TranslatorService, e.g. due to config changes.
     */
    public static void resetTranslator() {
        translator = null;
    }

    public static void init() {
        CachingTranslationService.loadTranslationCache();
    }

    public static void shutdown() {
        CachingTranslationService.saveTranslationCache();
    }

    public enum TranslationServices {
        GOOGLEAPI(GoogleApiTranslationService.class),
        PIGLATIN(PigLatinTranslationService.class);

        private final Class<? extends TranslationService> serviceClass;

        TranslationServices(Class<? extends TranslationService> serviceClass) {
            this.serviceClass = serviceClass;
        }
    }

    /**
     * A demo "translation" service that ignores the selected language, and always translates
     * to "pig latin". Use for test purposes, or for hours of enjoyment for the simple-minded. ;-)
     */
    public static class PigLatinTranslationService implements TranslationService {
        @Override
        public void translate(String message, String toLanguage, Consumer<String> handleTranslation) {
            StringBuilder latinString = new StringBuilder();
            if (message != null && !message.isEmpty()) {
                for (String word : message.split("\\s")) {
                    if (word.isEmpty()) continue;
                    if ("AEIOUaeiou".indexOf(word.charAt(0)) != -1) {
                        latinString.append(word).append("ay ");
                    } else {
                        latinString.append(word.substring(1)).append(word.charAt(0)).append("ay ");
                    }
                }
            }
            Utils.runAsync(() -> handleTranslation.accept(latinString.toString()));
        }
    }

}
