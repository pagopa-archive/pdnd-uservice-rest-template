CREATE KEYSPACE pdnd_uservice_rest_template_persistence_pet WITH replication = {'class': 'NetworkTopologyStrategy', 'dc1': '3'}  AND durable_writes = true;

CREATE TABLE pdnd_uservice_rest_template_persistence_pet.tag_views (
    tag_name text,
    timebucket bigint,
    timestamp timeuuid,
    persistence_id text,
    tag_pid_sequence_nr bigint,
    event blob,
    event_manifest text,
    meta blob,
    meta_ser_id int,
    meta_ser_manifest text,
    sequence_nr bigint,
    ser_id int,
    ser_manifest text,
    writer_uuid text,
    PRIMARY KEY ((tag_name, timebucket), timestamp, persistence_id, tag_pid_sequence_nr)
) WITH CLUSTERING ORDER BY (timestamp ASC, persistence_id ASC, tag_pid_sequence_nr ASC)
    AND bloom_filter_fp_chance = 0.01
    AND caching = {'keys': 'ALL', 'rows_per_partition': 'NONE'}
    AND comment = ''
    AND compaction = {'bucket_high': '1.5', 'bucket_low': '0.5', 'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy', 'enabled': 'true', 'max_threshold': '32', 'min_sstable_size': '50', 'min_threshold': '4', 'tombstone_compaction_interval': '86400', 'tombstone_threshold': '0.2', 'unchecked_tombstone_compaction': 'false'}
    AND compression = {'chunk_length_in_kb': '64', 'class': 'org.apache.cassandra.io.compress.LZ4Compressor'}
    AND crc_check_chance = 1.0
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 0
    AND gc_grace_seconds = 864000
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99PERCENTILE';

CREATE TABLE pdnd_uservice_rest_template_persistence_pet.messages (
    persistence_id text,
    partition_nr bigint,
    sequence_nr bigint,
    timestamp timeuuid,
    event blob,
    event_manifest text,
    meta blob,
    meta_ser_id int,
    meta_ser_manifest text,
    ser_id int,
    ser_manifest text,
    tags set<text>,
    timebucket text,
    writer_uuid text,
    PRIMARY KEY ((persistence_id, partition_nr), sequence_nr, timestamp)
) WITH CLUSTERING ORDER BY (sequence_nr ASC, timestamp ASC)
    AND bloom_filter_fp_chance = 0.01
    AND caching = {'keys': 'ALL', 'rows_per_partition': 'NONE'}
    AND comment = ''
    AND compaction = {'bucket_high': '1.5', 'bucket_low': '0.5', 'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy', 'enabled': 'true', 'max_threshold': '32', 'min_sstable_size': '50', 'min_threshold': '4', 'tombstone_compaction_interval': '86400', 'tombstone_threshold': '0.2', 'unchecked_tombstone_compaction': 'false'}
    AND compression = {'chunk_length_in_kb': '64', 'class': 'org.apache.cassandra.io.compress.LZ4Compressor'}
    AND crc_check_chance = 1.0
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 0
    AND gc_grace_seconds = 864000
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99PERCENTILE';

CREATE TABLE pdnd_uservice_rest_template_persistence_pet.tag_scanning (
    persistence_id text PRIMARY KEY,
    sequence_nr bigint
) WITH bloom_filter_fp_chance = 0.01
    AND caching = {'keys': 'ALL', 'rows_per_partition': 'NONE'}
    AND comment = ''
    AND compaction = {'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy', 'max_threshold': '32', 'min_threshold': '4'}
    AND compression = {'chunk_length_in_kb': '64', 'class': 'org.apache.cassandra.io.compress.LZ4Compressor'}
    AND crc_check_chance = 1.0
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 0
    AND gc_grace_seconds = 864000
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99PERCENTILE';

CREATE TABLE pdnd_uservice_rest_template_persistence_pet.all_persistence_ids (
    persistence_id text PRIMARY KEY
) WITH bloom_filter_fp_chance = 0.01
    AND caching = {'keys': 'ALL', 'rows_per_partition': 'NONE'}
    AND comment = ''
    AND compaction = {'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy', 'max_threshold': '32', 'min_threshold': '4'}
    AND compression = {'chunk_length_in_kb': '64', 'class': 'org.apache.cassandra.io.compress.LZ4Compressor'}
    AND crc_check_chance = 1.0
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 0
    AND gc_grace_seconds = 864000
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99PERCENTILE';

CREATE TABLE pdnd_uservice_rest_template_persistence_pet.offset_store (
    projection_name text,
    partition int,
    projection_key text,
    last_updated timestamp,
    manifest text,
    offset text,
    PRIMARY KEY ((projection_name, partition), projection_key)
) WITH CLUSTERING ORDER BY (projection_key ASC)
    AND bloom_filter_fp_chance = 0.01
    AND caching = {'keys': 'ALL', 'rows_per_partition': 'NONE'}
    AND comment = ''
    AND compaction = {'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy', 'max_threshold': '32', 'min_threshold': '4'}
    AND compression = {'chunk_length_in_kb': '64', 'class': 'org.apache.cassandra.io.compress.LZ4Compressor'}
    AND crc_check_chance = 1.0
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 0
    AND gc_grace_seconds = 864000
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99PERCENTILE';

CREATE TABLE pdnd_uservice_rest_template_persistence_pet.tag_write_progress (
    persistence_id text,
    tag text,
    offset timeuuid,
    sequence_nr bigint,
    tag_pid_sequence_nr bigint,
    PRIMARY KEY (persistence_id, tag)
) WITH CLUSTERING ORDER BY (tag ASC)
    AND bloom_filter_fp_chance = 0.01
    AND caching = {'keys': 'ALL', 'rows_per_partition': 'NONE'}
    AND comment = ''
    AND compaction = {'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy', 'max_threshold': '32', 'min_threshold': '4'}
    AND compression = {'chunk_length_in_kb': '64', 'class': 'org.apache.cassandra.io.compress.LZ4Compressor'}
    AND crc_check_chance = 1.0
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 0
    AND gc_grace_seconds = 864000
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99PERCENTILE';

CREATE TABLE pdnd_uservice_rest_template_persistence_pet.metadata (
    persistence_id text PRIMARY KEY,
    deleted_to bigint,
    properties map<text, text>
) WITH bloom_filter_fp_chance = 0.01
    AND caching = {'keys': 'ALL', 'rows_per_partition': 'NONE'}
    AND comment = ''
    AND compaction = {'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy', 'max_threshold': '32', 'min_threshold': '4'}
    AND compression = {'chunk_length_in_kb': '64', 'class': 'org.apache.cassandra.io.compress.LZ4Compressor'}
    AND crc_check_chance = 1.0
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 0
    AND gc_grace_seconds = 864000
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99PERCENTILE';
	
CREATE KEYSPACE pdnd_uservice_rest_template_persistence_pet_snap WITH replication = {'class': 'NetworkTopologyStrategy', 'dc1': '3'}  AND durable_writes = true;

CREATE TABLE pdnd_uservice_rest_template_persistence_pet_snap.snapshots (
    persistence_id text,
    sequence_nr bigint,
    meta blob,
    meta_ser_id int,
    meta_ser_manifest text,
    ser_id int,
    ser_manifest text,
    snapshot blob,
    snapshot_data blob,
    timestamp bigint,
    PRIMARY KEY (persistence_id, sequence_nr)
) WITH CLUSTERING ORDER BY (sequence_nr DESC)
    AND bloom_filter_fp_chance = 0.01
    AND caching = {'keys': 'ALL', 'rows_per_partition': 'NONE'}
    AND comment = ''
    AND compaction = {'bucket_high': '1.5', 'bucket_low': '0.5', 'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy', 'enabled': 'true', 'max_threshold': '32', 'min_sstable_size': '50', 'min_threshold': '4', 'tombstone_compaction_interval': '86400', 'tombstone_threshold': '0.2', 'unchecked_tombstone_compaction': 'false'}
    AND compression = {'chunk_length_in_kb': '64', 'class': 'org.apache.cassandra.io.compress.LZ4Compressor'}
    AND crc_check_chance = 1.0
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 0
    AND gc_grace_seconds = 864000
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99PERCENTILE';

CREATE KEYSPACE pdnd_uservice_rest_template_persistence_pet_proj WITH replication = {'class': 'NetworkTopologyStrategy', 'dc1': '3'}  AND durable_writes = true;

CREATE TABLE pdnd_uservice_rest_template_persistence_pet_proj.projection_management (
    projection_name text,
    partition int,
    projection_key text,
    last_updated timestamp,
    paused boolean,
    PRIMARY KEY ((projection_name, partition), projection_key)
) WITH CLUSTERING ORDER BY (projection_key ASC)
    AND bloom_filter_fp_chance = 0.01
    AND caching = {'keys': 'ALL', 'rows_per_partition': 'NONE'}
    AND comment = ''
    AND compaction = {'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy', 'max_threshold': '32', 'min_threshold': '4'}
    AND compression = {'chunk_length_in_kb': '64', 'class': 'org.apache.cassandra.io.compress.LZ4Compressor'}
    AND crc_check_chance = 1.0
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 0
    AND gc_grace_seconds = 864000
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99PERCENTILE';

CREATE TABLE pdnd_uservice_rest_template_persistence_pet_proj.offset_store (
    projection_name text,
    partition int,
    projection_key text,
    last_updated timestamp,
    manifest text,
    offset text,
    PRIMARY KEY ((projection_name, partition), projection_key)
) WITH CLUSTERING ORDER BY (projection_key ASC)
    AND bloom_filter_fp_chance = 0.01
    AND caching = {'keys': 'ALL', 'rows_per_partition': 'NONE'}
    AND comment = ''
    AND compaction = {'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy', 'max_threshold': '32', 'min_threshold': '4'}
    AND compression = {'chunk_length_in_kb': '64', 'class': 'org.apache.cassandra.io.compress.LZ4Compressor'}
    AND crc_check_chance = 1.0
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 0
    AND gc_grace_seconds = 864000
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99PERCENTILE';	
