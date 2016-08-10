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

* Move to 23
* Replace MoviedbFetcher with retrofit
* Add a sql database caching with a db cursor.
