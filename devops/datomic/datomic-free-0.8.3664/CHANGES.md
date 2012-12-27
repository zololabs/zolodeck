<!-- -*- mode: markdown ; mode: visual-line ; coding: utf-8 -*- -->

# Changes to Datomic

## Changed in 0.8.3664

* Added gap-detection validation when reading log on startup. 

Operating with a gap, while not resulting in loss of data, can cause
violations of uniqueness and cardinality constraints. Users on
releases prior to 0.8.3662 are strongly encouraged to move to 0.8.3662
or later as soon as possible.

## Changed in 0.8.3655

* Fixed bug where entities could have more than one :db/fn attribute.

* Fixed bug in log loading where a window of data could be invisible
  on restart, even though that data is present in the log.

## Changed in 0.8.3646

* Fixed bug where database indexing fails after a backup/restore
  cycle.

* Fixed bug in Clojure API where transaction futures sometimes would
  return (rather than throw) an exception.

* Fixed bug in Java API where transaction futures would occasionally
  return an object that is not the documented map.

* Better error reporting for some kinds of invalid queries.

* alpha support for CORS in the REST service. Note that the
  bin/rest args have changed. See http://docs.datomic.com/rest.html
  for details. 

## Changed in 0.8.3627

* Fixed bug where first restore of a database to a new storage did
  not include the most recent data, even though that data was present
  in the backup. (Subsequent restores were unaffected).

* Queries now take a :with clause, to specify variables to be kept in
  the aggregation set but not returned.

* Database.filter predicates now take two arguments: the unfiltered
  Database value and the Datom.

* You can now retrieve the Database that is the basis of an entity
  with Entity.db.

* You can now install a release of Datomic Pro into your local maven
  repository with bin/maven-install.

## Changed in 0.8.3619

* Alpha release of Database.filter, which returns a value of the
  database filtered to contain only the datoms satisfying a predicate.

* New AWS metric: IndexWrites.

* Peers no longer need to include a Datomic-specific maven repository,
  as Fressian (http://fressian.org) is now available from Maven
  Central and clojars.

## Changed in 0.8.3611

* Added memory-index-max setting to allow higher throughput for
  e.g. import jobs. See 
  http://support.datomic.com/customer/portal/articles/850962-handling-high-write-volumes
  for details.

* Bugfix: Fixed bug that prevents indexing jobs from completing with
  some usages of fulltext attributes.

* Added additional AWS instance types to AMI setup scripts.

* Bugfix: Cloudformation generation now respects
  aws-autoscaling-group-size setting.

* Fixed broken query example in GettingStarted.java.

* Fixed docstring for datomic.api/with.

## Changed in 0.8.3599

* Fixed "No suitable driver" error with dev: and free: protocols in
  some versions of Tomcat.

* Updated bin/datomic `delete-cf-stack` command to work with
  multiregion AWS support.

## Changed in 0.8.3595

* Fixed bug that prevented building queries from Java data.

* Transactor AMIs are now available in all AWS regions that support
  DynamoDB: us-east-1, us-west-1, us-west-2, eu-west-1,
  ap-northeast-1. and ap-southeast-1.

* Breaking change: CloudFormation properties file has a new required
  key `aws-region`, allowing you to select the AWS region where a
  transactor will run.

## Changed in 0.8.3591

* Preliminary support for Couchbase and Riak storages.

* Breaking change: DynamoDB storage is now region-aware. URIs
  include the AWS region as a first component. The transactor
  properties file has a new mandatory property `aws-dynamodb-region`.

* Breaking change: CloudWatch monitoring is now region-aware. If using
  CloudWatch, you must set `aws-cloudwatch-region` in transactor
  properties.

* Transactions now return a datomic.ListenableFuture, allowing a
  callback on transaction completion.

* Fixed bug in the command line entry point for restoring a database,
  which was defaulting to the most ancient backup instead of thre most
  recent.

* Fixed bug that prevented restoring to a `dev:` or `free:` storage in
  some situations.

## Changed in 0.8.3561

* Breaking change: db.with() now returns a map like the map returned
  from Connection.transact().

* Incompatible and unsupported schema changes now throw exceptions.

* Better error messages when calling query with bad or missing inputs.

* Documented system properties.

## Changes in 0.8.3551

* Fixes to alpha aggregation functions.

## Changes in 0.8.3546

* Alpha support for aggregation functions in query.

## Changes in 0.8.3538

* Fixed bug where variables bound by a query :in clause were not seen
  as bound inside rules.

* Added `invoke` API for invoking database functions.

## Changes in 0.8.3524

* Fixed bug that caused temporary ids to read incorrectly in
  transaction functions, causing transactions to fail.

## Changes in 0.8.3520

* Fixed but that prevented catalog page from loading on REST service when 
  running against a persistent storage.

* Enhancements to REST documentation.

## Changes in 0.8.3511

* The REST service is now its own documentation. Just point a browser at the root
  of the server:port on which you started the service. Note that the "web app"
  that results *is* the service. It is not an app built
  on the service, nor a set of documentation pages about the
  service. The URIs, query params, and POST data are the same ones you
  will use when accessing the service programmatically.

## Changes in 0.8.3488

* Initial version of REST service.

## Changes in 0.8.3479

* new API: `Entity.touch` touches all attributes of an entity, and any
  component entities recursively.

## Changes in 0.8.3470

* Fixed bug where some recursive queries return incorrect results.

* Fixed bug in command-line entry point for restoring from S3.

## Changes in 0.8.3460

* Fixed bug where peers could continue to interact with connections to
  deleted databases.

* Fixed query bug where constants in queries were not correctly joined
  with answers.

* Fixed directory structure in JAR file format, which was causing
  problems for some tools.

* Report serialization errors back to data function callers, rather
  than make them wait for transaction timeout.

* Better error messages for some common URI misspellings.

## Changes in 0.8.3438

* Fixed bug in :db/txInstant that prevented backdating before db was
created.

## Changes in 0.8.3435

* new API: `Peer.resolveTempid` provides the actual database ids
  corresponding to temporary ids submitted in a transaction.

* changed API: You can now explicitly specify the :db/txInstant of a
  transaction. This facilitates backdating transactions during data
  imports.

* changed API: Transaction futures now return a map with DB_BEFORE,
  DB_AFTER, TX_DATA and TEMPIDS.

* changed API: Transaction report queues now report the same data as
  calls to `Connection.transact`

* changed API: Transaction report TX_DATA now includes retractions in
   addition to assertions.

* new API: `Database.basisT` returns the t of the most recent
  transaction

* Bugfix: fixed bug that prevented AWS provisioning scripts from running

## Changes in 0.8.3423

* new API: `Database.history` returns a value of a database containing
  all assertions and retractions across time

* new API: `Database.isHistory` returns true if database is a history
  database

* new API: `Datom.added` returns true if datom is added, false if
  retracted

* changed API: the Datom relation in a query now exposes a fifth
  component, containing a boolean that is true for adds, false for
  retracts

* changed API: removed `Index` class, range capability now available
  directly from `Database`

* Bugfix: calling `keys` on an entity with no current attributes no
  longer throws NPE

* udpated to use recent (12.0.1) version of Google Guava

* when a peer calls `deleteDatabase`, shutdown that peer's connection

## Changes in 0.8.3397

* fixed bug where some recursive queries returned partial results

* simplified license key install: pro license keys are installed via a
  `license-key` entry in the transactor properties file

* connection catalog lookup is cached, so it is inexpensive to call
  `Peer.connect` as often as you like

* improved fulltext indexing and query performance

## Changes in 0.8.3372

* changed API: query clauses are considered in order

* changed API: when navigating entities, references to entities that
  have a `db/ident` return that ident, instead of the entity

* new API: `Database.index` supports range requests

* fixed broken dependency that prevented datomic-pro peer jar from
  working with DynamoDB

* new API: `Peer.part` function returns the partition of an entity id

* new API: `Peer.toTx` and `Peer.toT` conversion functions

* entity equality is ref-like, i.e. identity-based

* eliminated resource leaks that affected multiple databases and
  some failover scenarios

## Changes in 0.8.3343

* added API entry points for generating semi-sequential UUIDs, a.k.a
  squuids
    
* use correct maven artifact ids: `datomic-free` for Datomic Free
  Edition, and `datomic-pro` for Datomic Pro Edition

* fixed bug where queries could see retracted values of a
  cardinality-many attribute

## Changes in 0.8.3335

Initial public release of Datomic Free Edition and Datomic Pro Edition
