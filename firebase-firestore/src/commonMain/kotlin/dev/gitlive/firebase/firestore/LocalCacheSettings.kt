package dev.gitlive.firebase.firestore

public sealed interface LocalCacheSettings {

    public data class Persistent internal constructor(public val sizeBytes: Long) : LocalCacheSettings {

        public companion object {
            public fun newBuilder(): Builder = Builder()
        }

        public class Builder internal constructor() {
            public var sizeBytes: Long = FirebaseFirestoreSettings.DEFAULT_CACHE_SIZE_BYTES
            public fun build(): Persistent = Persistent(sizeBytes)
        }
    }
    public data class Memory internal constructor(val garbaseCollectorSettings: MemoryGarbageCollectorSettings) : LocalCacheSettings {

        public companion object {
            public fun newBuilder(): Builder = Builder()
        }

        public class Builder internal constructor() {

            public var gcSettings: MemoryGarbageCollectorSettings = MemoryGarbageCollectorSettings.Eager.newBuilder().build()

            public fun build(): Memory = Memory(gcSettings)
        }
    }
}

public typealias PersistentCacheSettings = LocalCacheSettings.Persistent
public typealias MemoryCacheSettings = LocalCacheSettings.Memory

public sealed interface MemoryGarbageCollectorSettings {
    public data object Eager : MemoryGarbageCollectorSettings {

        public fun newBuilder(): Builder = Builder()

        public class Builder internal constructor() {
            public fun build(): Eager = Eager
        }
    }
    public data class LRUGC internal constructor(val sizeBytes: Long) : MemoryGarbageCollectorSettings {

        public companion object {
            public fun newBuilder(): Builder = Builder()
        }

        public class Builder internal constructor() {
            public var sizeBytes: Long = FirebaseFirestoreSettings.DEFAULT_CACHE_SIZE_BYTES
            public fun build(): LRUGC = LRUGC(sizeBytes)
        }
    }
}

public typealias MemoryEagerGcSettings = MemoryGarbageCollectorSettings.Eager
public typealias MemoryLruGcSettings = MemoryGarbageCollectorSettings.LRUGC

public fun memoryCacheSettings(builder: LocalCacheSettings.Memory.Builder.() -> Unit): LocalCacheSettings.Memory = LocalCacheSettings.Memory.newBuilder().apply(builder).build()

public fun memoryEagerGcSettings(builder: MemoryGarbageCollectorSettings.Eager.Builder.() -> Unit): MemoryGarbageCollectorSettings.Eager = MemoryGarbageCollectorSettings.Eager.newBuilder().apply(builder).build()

public fun memoryLruGcSettings(builder: MemoryGarbageCollectorSettings.LRUGC.Builder.() -> Unit): MemoryGarbageCollectorSettings.LRUGC = MemoryGarbageCollectorSettings.LRUGC.newBuilder().apply(builder).build()

public fun persistentCacheSettings(builder: LocalCacheSettings.Persistent.Builder.() -> Unit): LocalCacheSettings.Persistent = LocalCacheSettings.Persistent.newBuilder().apply(builder).build()
