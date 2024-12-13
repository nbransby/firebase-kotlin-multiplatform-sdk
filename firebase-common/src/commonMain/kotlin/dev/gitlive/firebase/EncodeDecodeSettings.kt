package dev.gitlive.firebase

import kotlinx.serialization.modules.SerializersModule

/**
 * Settings used to configure encoding/decoding
 */
public sealed interface EncodeDecodeSettings {

    /**
     * The [SerializersModule] to use for serialization. This allows for polymorphic serialization on runtime
     */
    public val serializersModule: SerializersModule
}

/**
 * [EncodeDecodeSettings] used when encoding an object
 * @property encodeDefaults if `true` this will explicitly encode elements even if they are their default value
 */
public interface EncodeSettings : EncodeDecodeSettings {

    public val encodeDefaults: Boolean

    public interface Builder {
        public var encodeDefaults: Boolean
        public var serializersModule: SerializersModule
    }
}

public fun EncodeSettings.Builder.copyFrom(other: EncodeSettings.Builder) {
    encodeDefaults = other.encodeDefaults
    serializersModule = other.serializersModule
}

/**
 * [EncodeDecodeSettings] used when decoding an object
 * @param serializersModule the [SerializersModule] to use for deserialization. This allows for polymorphic serialization on runtime
 */
public interface DecodeSettings : EncodeDecodeSettings {

    public interface Builder {
        public var serializersModule: SerializersModule
    }
}

public fun DecodeSettings.Builder.copyFrom(other: DecodeSettings.Builder) {
    serializersModule = other.serializersModule
}

public interface EncodeDecodeSettingsBuilder :
    EncodeSettings.Builder,
    DecodeSettings.Builder
