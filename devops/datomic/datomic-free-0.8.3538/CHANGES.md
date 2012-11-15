<!-- -*- mode: markdown ; mode: visual-line ; coding: utf-8 -*- -->

# Changes to Datomic

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
