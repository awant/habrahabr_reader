# habrahabr_reader
Configurable rss in telegram for habrahabr

### The goal 
The goal is to implement a reader of habrahabr posts with configurable filters. It can help to get rid of useless articles and concentrate on important ones.

### Components:
1. A reader of new articles (in the background)
  * Check new articles
  * Check for articles updates
2. A storage of users and articles with a history of their updates (add/update/delete)
  * Store users with their settings
  * Store articles form habrahabr
3. A requester, which is built on top of telegram api. It applies settings for users and responsible for new articles

### Todo

* [] parsing for RSS (example in tests/resources)
* [] saving posts
* [] Actor for sending messages to telegram
* [] formatting post for telegram (removing html tags like <b>)

### Useful links:

* [awesome-scala list](https://github.com/lauris/awesome-scala)
* [eax.me about akka](https://eax.me/tag/akka/) 
