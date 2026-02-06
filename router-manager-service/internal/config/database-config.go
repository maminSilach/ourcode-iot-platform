package config

import (
	"database/sql"
	"fmt"
)

type DatabaseConfigLoader struct {
	config *Config
}

func New(cfg *Config) *DatabaseConfigLoader {
	return &DatabaseConfigLoader{
		config: cfg,
	}
}

func (db *DatabaseConfigLoader) ConnectionString() string {
	return fmt.Sprintf(
		"host=%s port=%d user=%s password=%s dbname=%s sslmode=%s",
		db.config.Database.Host,
		db.config.Database.Port,
		db.config.Database.User,
		db.config.Database.Password,
		db.config.Database.Name,
		db.config.Database.SSLMode)
}

func (db *DatabaseConfigLoader) Connect() (*sql.DB, error) {
	dbConn, err := sql.Open("postgres", db.ConnectionString())
	if err != nil {
		return nil, fmt.Errorf("failed to open database: %w", err)
	}

	if err := dbConn.Ping(); err != nil {
		return nil, fmt.Errorf("database ping failed: %w", err)
	}

	return dbConn, nil
}
