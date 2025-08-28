CREATE KEYSPACE IF NOT EXISTS devicekeyspace
    WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1};

USE devicekeyspace;