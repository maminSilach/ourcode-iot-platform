package config

import (
	"os"

	"gopkg.in/yaml.v3"
)

type Config struct {
	Env        string           `yaml:"env"`
	Database   DatabaseConfig   `yaml:"database"`
	Migrations MigrationsConfig `yaml:"migrations"`
	Server     ServerConfig     `yaml:"server"`
}

type DatabaseConfig struct {
	Host     string `yaml:"host"`
	Port     int    `yaml:"port"`
	User     string `yaml:"user"`
	Password string `yaml:"password"`
	Name     string `yaml:"name"`
	SSLMode  string `yaml:"sslmode"`
}

type MigrationsConfig struct {
	Path string `yaml:"path"`
}

type ServerConfig struct {
	Port    int    `yaml:"port"`
	Timeout string `yaml:"timeout"`
}

const (
	EnvLocal = "local"
	EnvProd  = "production"
)

func Load(configPath string) (*Config, error) {
	data, err := os.ReadFile(configPath)
	if err != nil {
		return nil, err
	}

	expandedData := expandEnvVars(data)

	var config Config
	if err := yaml.Unmarshal([]byte(expandedData), &config); err != nil {
		return nil, err
	}

	if config.Env == "" {
		config.Env = EnvLocal
	}

	return &config, nil
}

func expandEnvVars(data []byte) string {
	return os.Expand(string(data), func(key string) string {
		if val, ok := os.LookupEnv(key); ok {
			return val
		}

		return ""
	})
}
