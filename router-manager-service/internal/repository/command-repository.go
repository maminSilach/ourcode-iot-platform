package repository

import (
	"context"
	"database/sql"
	"fmt"
	"router-manager/internal/model"
	pb "router-manager/internal/proto/api/v1"
	"strings"
	"time"
)

type CommandRepositoryInterface interface {
	CreateCommand(commandToSave *model.CommandModel) error
	PollCommands(routerID string) ([]*pb.Command, error)
	AckCommand(routerID, commandID string) error
}

type CommandRepository struct {
	db *sql.DB
}

func NewCommandRepository(db *sql.DB) CommandRepositoryInterface {
	return &CommandRepository{db: db}
}

func (r *CommandRepository) CreateCommand(commandToSave *model.CommandModel) error {
	tx, err := r.db.BeginTx(context.Background(), &sql.TxOptions{
		Isolation: sql.LevelRepeatableRead,
	})

	if err != nil {
		return err
	}

	defer tx.Rollback()

	var uuidStr string
	var lastSeenErr error
	if commandToSave.RouterID != nil {
		uuidStr = commandToSave.RouterID.String()
		lastSeenErr = updateLastSeenRouters(tx, []string{uuidStr})
	} else {
		lastSeenErr = updateAllLastSeenRouters(tx)
	}

	if lastSeenErr != nil {
		return err
	}

	query := `
        INSERT INTO commands (id, router_id, command_type, payload, status, created_at)
        VALUES ($1, $2, $3, $4, $5, $6)
    `

	_, err = r.db.Exec(query,
		commandToSave.GetID(),
		commandToSave.GetRouterID(),
		commandToSave.GetCommandAsString(),
		commandToSave.GetPayload(),
		commandToSave.GetStatusAsString(),
		commandToSave.GetCreatedAtString())

	if err != nil {
		return err
	}

	if err := tx.Commit(); err != nil {
		return err
	}

	return err
}

func (r *CommandRepository) PollCommands(routerID string) ([]*pb.Command, error) {

	tx, err := r.db.BeginTx(context.Background(), &sql.TxOptions{
		Isolation: sql.LevelRepeatableRead,
	})
	if err != nil {
		return nil, err
	}

	defer tx.Rollback()

	var commands []*pb.Command
	var pollErr error
	var lastSeenErr error

	if routerID != "" {
		commands, pollErr = r.pollCommandsInTx(tx, routerID)

		if pollErr == nil {
			lastSeenErr = updateLastSeenRouters(tx, []string{routerID})
		} else {
			return nil, pollErr
		}

	} else {
		commands, pollErr = r.pollAllCommandsInTx(tx)
		if pollErr == nil {
			lastSeenErr = updateAllLastSeenRouters(tx)
		} else {
			return nil, pollErr
		}
	}

	if lastSeenErr != nil {
		return nil, pollErr
	}

	if len(commands) == 0 {
		tx.Rollback()
		return []*pb.Command{}, nil
	}

	err = r.updateCommandsInTx(tx, commands)
	if err != nil {
		return nil, err
	}

	if err := tx.Commit(); err != nil {
		return nil, err
	}

	return commands, nil
}

func (r *CommandRepository) AckCommand(routerID string, commandID string) error {
	query := `
	UPDATE commands c
		SET status = $1, acked_at = now()
	WHERE c.id = $2 AND c.router_id = $3 AND c.status = 'SENT'
    `

	_, err := r.db.Exec(query, pb.Status_ACKED.String(), commandID, routerID)

	return err
}

func (r *CommandRepository) pollCommandsInTx(tx *sql.Tx, routerID string) ([]*pb.Command, error) {
	query := `
	SELECT     
	    c.id,
	    c.router_id,
	    c.command_type,
	    c.payload,
	    c.sent_at,
	    c.acked_at,
	    c.status,
	    c.created_at 
	FROM commands c 
	    JOIN routers r ON r.id = c.router_id
	WHERE c.router_id = $1 AND (status = $2 OR status = $3)
    `

	rows, err := tx.Query(query, routerID, pb.Status_PENDING.String(), pb.Status_SENT.String())
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var commands []*pb.Command

	for rows.Next() {
		var (
			id          string
			routerID    string
			commandType string
			payload     string
			sentAt      sql.NullTime
			ackedAt     sql.NullTime
			status      string
			createdAt   time.Time
		)

		err := rows.Scan(
			&id,
			&routerID,
			&commandType,
			&payload,
			&sentAt,
			&ackedAt,
			&status,
			&createdAt,
		)
		if err != nil {
			return nil, err
		}

		cmdTypeValue, exists := pb.CommandType_value[commandType]
		if !exists {
			cmdTypeValue = 0
		}

		command := &pb.Command{
			Id:          id,
			RouterId:    routerID,
			CommandType: pb.CommandType(cmdTypeValue),
			Payload:     payload,
			Status:      pb.Status_SENT,
			CreatedAt:   createdAt.Format(time.RFC3339),
			SentAt:      time.Now().Format(time.RFC3339),
		}

		commands = append(commands, command)
	}

	if err := rows.Err(); err != nil {
		return nil, err
	}

	return commands, nil
}

func (r *CommandRepository) updateCommandsInTx(tx *sql.Tx, commands []*pb.Command) error {
	if len(commands) == 0 {
		return nil
	}

	placeholders := make([]string, len(commands))
	args := make([]interface{}, len(commands))

	for i, cmd := range commands {
		placeholders[i] = fmt.Sprintf("$%d", i+1)
		args[i] = cmd.GetId()
	}

	statusParamPosition := len(commands) + 1

	query := fmt.Sprintf(`
        UPDATE commands 
        SET 
            status = $%d,
            sent_at = NOW()
        WHERE id IN (%s)
    `, statusParamPosition, strings.Join(placeholders, ", "))

	args = append(args, pb.Status_SENT.String())
	_, err := tx.Exec(query, args...)

	return err
}

func (r *CommandRepository) pollAllCommandsInTx(tx *sql.Tx) ([]*pb.Command, error) {
	query := `
	SELECT     
	    c.id,
	    c.router_id,
	    c.command_type,
	    c.payload,
	    c.sent_at,
	    c.acked_at,
	    c.status,
	    c.created_at 
	FROM commands c 
		WHERE status = $1 OR status = $2
    `

	rows, err := tx.Query(query, pb.Status_PENDING.String(), pb.Status_SENT.String())
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var commands []*pb.Command

	for rows.Next() {
		var (
			id          string
			routerID    sql.NullString
			commandType string
			payload     sql.NullString
			sentAt      sql.NullTime
			ackedAt     sql.NullTime
			status      string
			createdAt   time.Time
		)

		err := rows.Scan(
			&id,
			&routerID,
			&commandType,
			&payload,
			&sentAt,
			&ackedAt,
			&status,
			&createdAt,
		)
		if err != nil {
			return nil, err
		}

		cmdTypeValue, exists := pb.CommandType_value[commandType]
		if !exists {
			cmdTypeValue = 0
		}

		routerIDValue := getStringFromNull(routerID)
		payloadValue := getStringFromNull(routerID)

		command := &pb.Command{
			Id:          id,
			RouterId:    routerIDValue,
			CommandType: pb.CommandType(cmdTypeValue),
			Payload:     payloadValue,
			Status:      pb.Status_SENT,
			CreatedAt:   createdAt.Format(time.RFC3339),
			SentAt:      time.Now().Format(time.RFC3339),
		}

		commands = append(commands, command)
	}

	if err := rows.Err(); err != nil {
		return nil, err
	}

	return commands, nil
}

func updateLastSeenRouters(tx *sql.Tx, routerIds []string) error {
	if len(routerIds) == 0 {
		return nil
	}

	placeholders := make([]string, len(routerIds))
	args := make([]interface{}, len(routerIds))

	for i, routerId := range routerIds {
		placeholders[i] = fmt.Sprintf("$%d", i+1)
		args[i] = routerId
	}

	query := fmt.Sprintf(`
        UPDATE routers 
        	SET last_seen_at = NOW()
        WHERE id IN (%s)
    `, strings.Join(placeholders, ", "))
	_, err := tx.Exec(query, args...)

	return err
}

func updateAllLastSeenRouters(tx *sql.Tx) error {
	query := `
        UPDATE routers 
        	SET last_seen_at = NOW()
    `

	_, err := tx.Exec(query)

	return err
}

func getStringFromNull(value sql.NullString) string {
	if value.Valid {
		return value.String
	} else {
		return ""
	}
}
