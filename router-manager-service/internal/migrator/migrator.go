package migrator

import (
	"database/sql"
	"fmt"
	"log"
	"router-manager/internal/config"

	"github.com/golang-migrate/migrate/v4"
	"github.com/golang-migrate/migrate/v4/database/postgres"
)

type Migrator struct {
	config *config.Config
}

func New(cfg *config.Config) *Migrator {
	return &Migrator{
		config: cfg,
	}
}

func (ms *Migrator) ApplyMigrations() {

	connStr := fmt.Sprintf(
		"host=%s port=%d user=%s password=%s dbname=%s sslmode=%s",
		ms.config.Database.Host,
		ms.config.Database.Port,
		ms.config.Database.User,
		ms.config.Database.Password,
		ms.config.Database.Name,
		ms.config.Database.SSLMode)

	db, err := sql.Open("postgres", connStr)
	if err != nil {
		log.Fatal("Failed to connect to database:", err)
	}

	defer db.Close()

	if err = db.Ping(); err != nil {
		log.Fatal("Database is not reachable:", err)
	}

	driver, err := postgres.WithInstance(db, &postgres.Config{})
	if err != nil {
		log.Fatal("Failed to create migration driver:", err)
	}

	sourceURL := fmt.Sprintf("file://%s", ms.config.Migrations.Path)
	m, err := migrate.NewWithDatabaseInstance(
		sourceURL,
		ms.config.Database.Name, driver)

	if err != nil {
		log.Fatal("Failed to create migration instance:", err)
	}

	if err := m.Up(); err != nil && err != migrate.ErrNoChange {
		log.Fatal("Migration failed:", err)
	}
}
