package com.selesse.steam.appcache;

// Controls a single pass over the app cache: which entries actually get parsed (the rest have
// their bytes skipped unread), what happens to a parsed entry, and when the pass can stop early.
// read() and readSome()/readOne() differ only in which AppQuery they use - the scanning,
// skipping, and error-handling logic around it is otherwise identical and lives once in
// AppCacheBufferedReader.scanAppEntries.
interface AppQuery {
    boolean shouldParse(int appId);

    void onParsed(int appId, App app);

    boolean isDone();
}
