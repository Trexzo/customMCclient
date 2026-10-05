package dev.trexzo.custommc.core.setting;

public interface SettingCodec<T> {
    String encode(T value);

    T decode(String encoded);
}
