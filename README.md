# Movies

This is project #2 from the udacity android nano degree program. The project requirements are
[online](https://goo.gl/EpF3N2).

The app source is divided into data, ui, and sync. I'll cover each in turn.

I am not an artsy GUI person, so this won't win any design awards. I took this as a learning
project to pick up how to write a ContentProvider, a SyncAdapter, various Fragment based
Activities that handle both the Activity and Fragment life cycles.  I took some care
  to make sure no database query() or insert() calls were made on the UI thread.

## Data

 The data section is an SQLite content provider. The database schema:

 ![Schema](./images/schema.png)

The three main lists that are displayed are favorites, popular, and toprated. In each case,
we return a movies inner joined with the list based on the `mid` in each table and
movies left joined with the favorites list. This allows the details view access to all of the
 data it needs with a single cursor.

Bulk inserts are supported within a single transaction, which speeds up android devices
considerably.

I try to minimize the number of moviedb api calls. We prefetch the popular and toprated lists
but don't prefetch reviews or trailers as this would require two api calls per movie. Instead
we fetch and cache reviews (`.data.ReviewLoader`) and trailers (`.data.TrailerLoader`) on demand.

Genre names are loaded and cached (`.data.GenreNameMapper`)from the moviedb once after
initial installation.

The `.data.Contract` class contains interfaces that describe the column names, indices, and
default projections in the joined tables returned on calls to `query()`. It also has static
 functions that return fluent builders for the `ContentValues` needed for `insert()`.


## Sync

The sync package is pretty simple.

The `.sync.StubAuthenticatorService`  that I can reuse in other projects. I should have put it
 in a generic package, but I can just change the package name when I reuse the class.

The `.sync.MoviesSyncService` is just a couple of lines of code to create a `MoviesSyncAdapter` or
 an `IBinder`.

`.sync.MoviesSyncAdapter` contains all of the real code for synchronization. The moviedb api
returns 20 movies per page when fetching the toprated or popular lists. By default,
`MoviesSyncAdapter` will fetch 5 pages at a time from each list up to a MAX_PAGE=50. Every
SYNC_FLEXTIME (6 hrs +- 3hrs)

## UI


## TODO




