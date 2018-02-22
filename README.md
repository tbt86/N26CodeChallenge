# N26CodeChallenge

This is my solution for the N26 coding challenge.
It implements a REST service, which is:
 * accepting transactions from the last 60 seconds
 * generating statistics for the last 60 seconds for these transactions

Just run 'mvn clean install' to run the tests and build it.

The complexity of the implementation for both use cases is O(1), both for time and space.
This is achieved by not storing the transactions themselves, but by storing their aggregated statistics per second.
All transactions are aggregated into 'buckets' of one second. So that we only need to store 60 of these statistics. Thus the space costs are constant.
When generating the statistics over the last 60 seconds, we just need to iterate over those 60 objects and add them together. This way the computational costs for the statistics are constant as well.
The statistics could also be cached, so that we don't need to recalculate them if nothing changed, but this wasn't implemented, as it would be rather simple to add.

Furthermore this solutions trades strict consistency for correctness. This implementation is only 'eventual consistent' and it might take up to one second for a transaction to show up in the generated statistics. But the implementation will never 'lose' any information. If you ask the service every 60 seconds for the statistics, you will get eventually the statistics including all transactions.
Let me explain this with a simplified example: If you ask for the statistics in second '60.250', this solution can conceptually only return either the statistics from 0 to 60 or from 1 to 60.250 (current time). This is because the transactions are organised in one second sized buckets to achieve O(1). Returning the transactions from 1 to 60.25 would be very recent, but only include transactions for 59.25 seconds and we 'lose' all the information from 0 to 1 second.
So this implementation will give you the statistics from 0 to 60 seconds instead, which is in total 60 seconds, but it will not include the transactions from 60.000 to 60.250 seconds yet. These transactions will be included if you ask for the statistics after second 61.

Additionally I have made the following assumptions while implementing this:
* Amount/Timestamp pairs for transactions are not unique and might repeat
* I assumed all timestamps are in UTC
* I am ignoring timestamps from the future. This was not part of the description.
* Timestamps will always come in milliseconds.
* If there are no transactions, the service will just return 204
