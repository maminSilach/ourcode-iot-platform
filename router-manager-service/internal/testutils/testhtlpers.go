package testutils

import (
	"context"
	"database/sql"
	"fmt"
	"log"
	"os"
	"path/filepath"
	"time"

	_ "github.com/jackc/pgx/v5/stdlib"
	"github.com/testcontainers/testcontainers-go"
	"github.com/testcontainers/testcontainers-go/modules/postgres"
	"github.com/testcontainers/testcontainers-go/wait"
)

type PostgresContainer struct {
	container *postgres.PostgresContainer
	db        *sql.DB
}

func NewPostgresContainer(ctx context.Context) (*PostgresContainer, error) {
	container, err := postgres.Run(ctx,
		"postgres:15-alpine",
		postgres.WithDatabase("router_manager_test"),
		postgres.WithUsername("test_user"),
		postgres.WithPassword("test_password"),
		testcontainers.WithWaitStrategy(
			wait.ForLog("database system is ready to accept connections").
				WithOccurrence(2).
				WithStartupTimeout(30*time.Second),
		),
	)
	if err != nil {
		return nil, fmt.Errorf("failed to start postgres container: %w", err)
	}

	connStr, err := container.ConnectionString(ctx)
	if err != nil {
		return nil, fmt.Errorf("failed to get connection string: %w", err)
	}

	db, err := sql.Open("pgx", connStr)
	if err != nil {
		return nil, fmt.Errorf("failed to open database connection: %w", err)
	}

	db.SetMaxOpenConns(5)
	db.SetMaxIdleConns(2)
	db.SetConnMaxLifetime(1 * time.Hour)

	if err := waitForDB(ctx, db); err != nil {
		return nil, fmt.Errorf("failed to wait for database: %w", err)
	}

	return &PostgresContainer{
		container: container,
		db:        db,
	}, nil
}

func waitForDB(ctx context.Context, db *sql.DB) error {
	ctx, cancel := context.WithTimeout(ctx, 30*time.Second)
	defer cancel()

	for {
		select {
		case <-ctx.Done():
			return ctx.Err()
		default:
			if err := db.PingContext(ctx); err == nil {
				return nil
			}
			time.Sleep(100 * time.Millisecond)
		}
	}
}

func (pc *PostgresContainer) GetDB() *sql.DB {
	return pc.db
}

func (pc *PostgresContainer) Close(ctx context.Context) error {
	if pc.db != nil {
		pc.db.Close()
	}
	if pc.container != nil {
		return pc.container.Terminate(ctx)
	}
	return nil
}

func (pc *PostgresContainer) RunMigrations(ctx context.Context, migrationsPath string) error {
	upMigrationPath := filepath.Join(migrationsPath, "1_init_database.up.sql")
	upSQL, err := os.ReadFile(upMigrationPath)
	if err != nil {
		return fmt.Errorf("failed to read up migration file: %w", err)
	}

	_, err = pc.db.ExecContext(ctx, string(upSQL))
	if err != nil {
		return fmt.Errorf("failed to execute migration: %w", err)
	}

	log.Println("Database migrations applied successfully")
	return nil
}

func (pc *PostgresContainer) Cleanup(ctx context.Context) error {
	_, err := pc.db.ExecContext(ctx, "TRUNCATE TABLE commands RESTART IDENTITY CASCADE;")
	return err
}
