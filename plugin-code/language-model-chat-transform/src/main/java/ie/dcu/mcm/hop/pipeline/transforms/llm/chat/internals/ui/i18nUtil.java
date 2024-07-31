package ie.dcu.mcm.hop.pipeline.transforms.llm.chat.internals.ui;

import ie.dcu.mcm.hop.pipeline.transforms.llm.chat.LanguageModelChatDialog;

import static org.apache.hop.i18n.BaseMessages.getString;

public class i18nUtil {

    private static final Class<?> PKG = LanguageModelChatDialog.class; // For Translator


    public static String i18n(String key) {
        return getString(PKG, key);
    }

    public static String i18n(String key, String... parameters) {
        return getString(PKG, key, parameters);
    }
}
