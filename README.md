# Movies

This is project #2 from the udacity android nano degree program. 

Some notes: 

* Starting with the android studio default empty template. I went with a min sdk level of 
19 because I want to be able to use Java 7 features (try with resources). 

* The main activity will be a single fragment hosting a RecyclerView with a GridLayoutManager. 

* I went with glide instead of picasso for image loading. Picasso is causing sigabrt
 when doing resizing on api24. api24 source hasn't been released yet, and glide didn't
 have  that problem.


TODO:

* Move to SDK 23
    - sdk 24 doesn't have source yet. This is a learning project,
    so sdk source is quite helpful.
* Replace MoviedbFetcher with retrofit
    -  I did this, but I'm not really happy with the results. Removed one class and the
    retrofit callback code is cool, but it is difficult to examine what is going on with
    annotations and retrofit adds a bit to apk size because it pulls in retrofit,
    support-v4, okio, okhttp, and converter-gson libraries.
* Create a content provider with an sql backend
    - I decided not to do this. The lists change daily, so long term caching isn't really
    going to happen. The data isn't shared with other apps and all we really need stored
    locally are the favorites lists, which aren't complicated enough to require sql. sql
    can be quite slow for getting a database and until ContentProvider has an onDestroy()
    hook, we really need to open and close on every insert. That is incredibly slow and
    unnecessarily tough on flash.