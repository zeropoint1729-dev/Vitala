# Gson deserializes these by matching JSON keys to field names via reflection.
# Without these keep rules, R8 renaming would silently break disease/news parsing
# in a minified build (fields would just come back null, no crash, no warning).
-keep class com.faridul.vitala.data.model.** { *; }
-keep class com.faridul.vitala.data.remote.PubMedSearchResponse { *; }
-keep class com.faridul.vitala.data.remote.EsearchResult { *; }
-keep class com.faridul.vitala.data.remote.PubMedSummaryResponse { *; }

-keepattributes Signature
-keepattributes *Annotation*

# Room
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**
