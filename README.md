Pillage ( Alpha. Still under development )
=======

A super lightweight stats and monitoring lib for the jvm. It provides counters, metrics, timers,
labels and jvm stats. The goals are to be lightweight, fast, make good use of resources and be 
flexible enough to easily hack on pieces when needed.

The library was put together by pillaging a number of other open source libs ( hence the name ) for 
their good parts and ideas then mixing them together with some new code and ideas.

Libraries pillaged thus far:

- [Ostrich](https://github.com/twitter/ostrich)
- [Perf4j](http://perf4j.codehaus.org/)
- [embeddedgmetric](http://code.google.com/p/embeddedgmetric/)

Components
----------
### Counters
Counters just count things. They can be incremented, read and reset.

### Metrics
Metrics are tracked with a Distribution. The the only current implementation is based on the Histogram
in the Ostrich scala lib. It provides max, min, mean within 5% and percitiles.

### Labels
Labels are just string values. This also came from Ostrich and can be used to set flags 
instead of holding statistical value.

### StatsSummary
A StatsSummary contains Counters, Metrics and Labels for a set amount of time. A summary is an immutable
object and will throw exceptions if you try to mess with it.

### StatsContainer
A StatsContainer is the client interface to interact with Pillage. Through this interface you can
increment counters, add metrics, set labels, aquire summaries, etc.

### StatsCollector
The StatsCollector interface is used to collect stats from the container for purposes of reporting 
or aggregation. A collector will cache a set of stats from a StatsSummary and when the collect() method
is called, it will aquire a new StatsSummary object and calculate the diff between the cached verison
and the new version and return that diff as a new StatsSummary object. The collect() method is 
how you can see change over time which is good for graphing.

The stats collector provides the option to include jvm stats which is off by default. If this option
is turned on then stats from the MBeans provided with java will be included the StatsSummarys. This is
a good way to get heap usage, threads counts, classes loaded/unloaded, gc counts etc without having to 
mess with JMX.

A stats collector can have listeners attached called StatsReporters that will receive the delta StatsSummary 
when collect() is called.

### StatsReporter
A StatsReporter has just one method report that takes in a StatsSummary. There is currently one implementation,
GangliaStatsReporter which will send the stats to a Ganlgia server over UDP.

### PeriodicStatsCollector
This is a utility singleton that can be used to schedule regular calls to collect() for a given StatsCollector.


------------------------------------------------

## TODO
+ create some other reporters ( logging, graphite )
+ make calls to report() non blocking. A call to report will most likely involve IO so if it hangs I don't
want it to cause a backup or other bad things.
+ add gauges through reflection. This is a really cool feature in Ostrich but since java doesn't have
closures it is a bit more difficult to implement.
+ standardize the stats naming, I think it is a bit all over the place right now.
+ enable PeriodicStatsCollector to be restarted. Right now if the thread dies the app will have to be restarted
to work on reporting.
+ try alternate metric backings. Histogram is cool but I would like to collect some other things like stddev, skew etc.
+ Profiled annotation like Perf4js
+ get some production usage and iterate!

