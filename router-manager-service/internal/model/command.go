package model

import (
	"database/sql"
	pb "router-manager/internal/proto/api/v1"
	"time"

	"router-manager/internal/util"

	"github.com/google/uuid"
	"github.com/jackc/pgx/v5/pgtype"
)

type CommandModel struct {
	ID          uuid.UUID
	RouterID    *uuid.UUID
	CommandType pb.CommandType
	Payload     *pgtype.Text
	Status      pb.Status
	SentAt      sql.NullTime
	AckedAt     sql.NullTime
	CreatedAt   time.Time
}

func (c *CommandModel) GetID() uuid.UUID {
	return c.ID
}

func (c *CommandModel) GetRouterID() *uuid.UUID {
	return c.RouterID
}

func (c *CommandModel) GetCommandType() pb.CommandType {
	return c.CommandType
}

func (c *CommandModel) GetCommandAsString() string {
	return c.CommandType.String()
}

func (c *CommandModel) GetPayload() *pgtype.Text {
	return c.Payload
}

func (c *CommandModel) GetStatus() pb.Status {
	return c.Status
}

func (c *CommandModel) GetStatusAsString() string {
	return c.Status.String()
}

func (c *CommandModel) GetSentAt() sql.NullTime {
	return c.SentAt
}

func (c *CommandModel) GetSentAtTime() *time.Time {
	if !c.SentAt.Valid {
		return nil
	}
	return &c.SentAt.Time
}

func (c *CommandModel) GetAckedAt() sql.NullTime {
	return c.AckedAt
}

func (c *CommandModel) GetAckedAtTime() *time.Time {
	if !c.AckedAt.Valid {
		return nil
	}
	return &c.AckedAt.Time
}

func (c *CommandModel) GetCreatedAt() time.Time {
	return c.CreatedAt
}

func (c *CommandModel) GetCreatedAtString() string {
	return c.CreatedAt.Format(time.RFC3339)
}

func ToCommandModel(req *pb.CommandRequest, status pb.Status) (*CommandModel, error) {
	parsedRouterID, err := util.ToUUIDFromStringIfPresent(req.GetRouterId())
	payloadAsJSONPGType, err := util.ToJSONPGType(req.GetPayload())

	c := &CommandModel{
		ID:          uuid.New(),
		RouterID:    parsedRouterID,
		CommandType: req.GetCommandType(),
		Payload:     payloadAsJSONPGType,
		Status:      status,
		SentAt: sql.NullTime{
			Time:  time.Now(),
			Valid: true,
		},
		CreatedAt: time.Now(),
	}

	return c, err
}
