package main

import (
	_ "database/sql"
	"flag"
	"log"
	"router-manager/internal/config"
	"router-manager/internal/grpc"
	"router-manager/internal/migrator"
	"router-manager/internal/repository"
	"router-manager/internal/service"

	_ "router-manager/internal/config"

	_ "github.com/golang-migrate/migrate/v4"
	_ "github.com/golang-migrate/migrate/v4/database/postgres"
	_ "github.com/golang-migrate/migrate/v4/source/file"
	_ "github.com/lib/pq"
)

func main() {
	configPath := flag.String("config", "", "Path to config file (required)")
	env := flag.String("env", "local", "Environment: local or production")
	flag.Parse()

	if *configPath == "" {
		switch *env {
		case "production":
			*configPath = "config/prod.yaml"
		default:
			*configPath = "config/local.yaml"
		}
	}

	cfg, err := config.Load(*configPath)
	if err != nil {
		log.Fatalf("Failed to load config from %s: %v", *configPath, err)
	}

	databaseConfig := config.New(cfg)
	connect, err := databaseConfig.Connect()
	if err != nil {
		log.Fatalf("Failed to connect to database: %v", err)
	}

	commandRepo := repository.NewCommandRepository(connect)
	commandService := service.NewCommandService(commandRepo)
	commandServiceController := grpc.NewCommandServiceController(commandService)

	migrator := migrator.New(cfg)
	migrator.ApplyMigrations()

	grpcServer := grpc.New(cfg, commandServiceController)
	grpcServer.Run()
}
