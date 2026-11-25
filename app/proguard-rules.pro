# Preserve ParsedContent and subclasses for stable type identity
-keep class com.HrshD1eux.Scan.parser.ParsedContent** { *; }
-keepclassmembers class com.HrshD1eux.Scan.parser.ParsedContent** { *; }

# Preserve HistoryEntity for Room SQLite mapping
-keep class com.HrshD1eux.Scan.history.HistoryEntity { *; }
-keepclassmembers class com.HrshD1eux.Scan.history.HistoryEntity { *; }
