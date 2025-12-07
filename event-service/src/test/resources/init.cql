CREATE KEYSPACE IF NOT EXISTS eventkeyspace
    WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1};

USE eventkeyspace;

CREATE TABLE IF NOT EXISTS device_events_by_device
(
    device_id text,
    event_id  text,
    timestamp bigint,
    type      text,
    payload   text,
    PRIMARY KEY ((device_id), event_id)
) WITH CLUSTERING ORDER BY (event_id ASC);

INSERT INTO eventkeyspace.device_events_by_device(device_id, event_id, timestamp, type) VALUES ('f09862fa-9d7e-4bb5-a574-2f1d0e3e2186', '500efbda-9621-4ec3-a80e-2ac049d61e6c', 10000, 'CL');
INSERT INTO eventkeyspace.device_events_by_device(device_id, event_id, timestamp, type) VALUES ('f09862fa-9d7e-4bb5-a574-2f1d0e3e2186', 'e21da112-7160-4f58-9e73-6d3de436d238', 100000, 'VS');
INSERT INTO eventkeyspace.device_events_by_device(device_id, event_id, timestamp, type) VALUES ('f09862fa-9d7e-4bb5-a574-2f1d0e3e2186', '243397f0-353d-4132-802d-5e77417c4f41', 1000, 'CL');