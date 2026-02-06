package util

import (
	"database/sql"
	"time"

	"github.com/google/uuid"
	"github.com/jackc/pgx/v5/pgtype"
)

func ToUUIDFromStringIfPresent(uuidString string) (*uuid.UUID, error) {
	if uuidString == "" {
		return nil, nil
	}

	parsedUUID, err := uuid.Parse(uuidString)
	if err != nil {
		return nil, err
	}

	return &parsedUUID, nil
}

func ToJSONPGType(jsonAsString string) (*pgtype.Text, error) {
	var payloadJSON pgtype.Text
	if jsonAsString != "" {
		payloadJSON = pgtype.Text{
			String: jsonAsString,
			Valid:  true,
		}
	} else {
		payloadJSON = pgtype.Text{Valid: false}
	}

	return &payloadJSON, nil
}

func StringToNullTime(timeStr string) (sql.NullTime, error) {
	if timeStr == "" {
		return sql.NullTime{Valid: false}, nil
	}

	parsedTime, err := time.Parse(time.RFC3339, timeStr)
	if err != nil {
		return sql.NullTime{Valid: false}, err
	}

	return sql.NullTime{
		Time:  parsedTime,
		Valid: true,
	}, nil
}
