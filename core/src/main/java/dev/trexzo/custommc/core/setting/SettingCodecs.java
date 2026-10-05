package dev.trexzo.custommc.core.setting;

public final class SettingCodecs {
    public static final SettingCodec<String> STRING =
            new SettingCodec<String>() {
                @Override
                public String encode(final String value) {
                    return value;
                }

                @Override
                public String decode(final String encoded) {
                    return encoded;
                }
            };

    public static final SettingCodec<Integer> INTEGER =
            new SettingCodec<Integer>() {
                @Override
                public String encode(final Integer value) {
                    return Integer.toString(value);
                }

                @Override
                public Integer decode(final String encoded) {
                    return Integer.valueOf(encoded);
                }
            };

    public static final SettingCodec<Double> DOUBLE =
            new SettingCodec<Double>() {
                @Override
                public String encode(final Double value) {
                    return Double.toString(value);
                }

                @Override
                public Double decode(final String encoded) {
                    return Double.valueOf(encoded);
                }
            };

    public static final SettingCodec<Boolean> BOOLEAN =
            new SettingCodec<Boolean>() {
                @Override
                public String encode(final Boolean value) {
                    return Boolean.toString(value);
                }

                @Override
                public Boolean decode(final String encoded) {
                    if ("true".equalsIgnoreCase(encoded)) {
                        return Boolean.TRUE;
                    }
                    if ("false".equalsIgnoreCase(encoded)) {
                        return Boolean.FALSE;
                    }
                    throw new IllegalArgumentException(
                            "invalid boolean: " + encoded);
                }
            };

    private SettingCodecs() {
    }
}
