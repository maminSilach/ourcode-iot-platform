package grpc

import (
	"context"
	"database/sql"
	"fmt"
	"testing"
	"time"

	pb "router-manager/internal/proto/api/v1"
	"router-manager/internal/repository"
	"router-manager/internal/service"
	"router-manager/internal/testutils"

	"github.com/google/uuid"
	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/suite"
	"google.golang.org/protobuf/types/known/emptypb"
)

type CommandControllerIntegrationTestSuite struct {
	suite.Suite
	pgContainer *testutils.PostgresContainer
	repo        repository.CommandRepositoryInterface
	service     service.CommandService
	controller  *CommandServiceController
	ctx         context.Context
}

func TestCommandControllerIntegrationTestSuite(t *testing.T) {
	if testing.Short() {
		t.Skip("Skipping integration tests in short mode")
	}
	suite.Run(t, new(CommandControllerIntegrationTestSuite))
}

func (suite *CommandControllerIntegrationTestSuite) SetupSuite() {
	suite.ctx = context.Background()

	container, err := testutils.NewPostgresContainer(suite.ctx)
	if err != nil {
		suite.T().Fatalf("Failed to start postgres container: %v", err)
	}
	suite.pgContainer = container

	err = suite.pgContainer.RunMigrations(suite.ctx, "../../database/migrations")
	if err != nil {
		suite.T().Fatalf("Failed to run migrations: %v", err)
	}

	db := suite.pgContainer.GetDB()
	suite.repo = repository.NewCommandRepository(db)
	suite.service = service.NewCommandService(suite.repo)
	suite.controller = NewCommandServiceController(suite.service)
}

func (suite *CommandControllerIntegrationTestSuite) TearDownSuite() {
	if suite.pgContainer != nil {
		err := suite.pgContainer.Close(suite.ctx)
		if err != nil {
			suite.T().Logf("Failed to close postgres container: %v", err)
		}
	}
}

func (suite *CommandControllerIntegrationTestSuite) SetupTest() {
	err := suite.pgContainer.Cleanup(suite.ctx)
	if err != nil {
		suite.T().Fatalf("Failed to cleanup test data: %v", err)
	}
}

func (suite *CommandControllerIntegrationTestSuite) TestSendCommand_Success() {
	// Arrange
	req := &pb.CommandRequest{
		CommandType: pb.CommandType_REBOOT,
	}

	// Act
	response, err := suite.controller.SendCommand(suite.ctx, req)

	// Assert
	assert.NoError(suite.T(), err)
	assert.NotNil(suite.T(), response)
	assert.IsType(suite.T(), &emptypb.Empty{}, response)

	db := suite.pgContainer.GetDB()
	var count int
	err = db.QueryRowContext(suite.ctx,
		"SELECT COUNT(*) FROM commands").Scan(&count)
	assert.NoError(suite.T(), err)
	assert.Equal(suite.T(), 1, count)
}

func (suite *CommandControllerIntegrationTestSuite) TestSendCommand_InvalidRouterID() {
	// Arrange
	req := &pb.CommandRequest{
		RouterId:    "invalid-uuid",
		CommandType: pb.CommandType_REBOOT,
	}

	// Act
	response, err := suite.controller.SendCommand(suite.ctx, req)

	// Assert
	assert.Error(suite.T(), err)
	assert.Nil(suite.T(), response)

	db := suite.pgContainer.GetDB()
	var count int
	err = db.QueryRowContext(suite.ctx, "SELECT COUNT(*) FROM commands").Scan(&count)
	assert.NoError(suite.T(), err)
	assert.Equal(suite.T(), 0, count)
}

func (suite *CommandControllerIntegrationTestSuite) TestPoll_Success() {
	// Arrange
	routerID := uuid.New()
	serialNumber := "RT-" + routerID.String()[:8]

	db := suite.pgContainer.GetDB()

	_, err := db.ExecContext(suite.ctx, `
		INSERT INTO routers (id, serial_number, last_seen_at, created_at)
		VALUES ($1, $2, $3, $4)
	`,
		routerID, serialNumber, time.Now(), time.Now(),
	)
	assert.NoError(suite.T(), err)

	command1ID := uuid.New()
	command2ID := uuid.New()

	_, err = db.ExecContext(suite.ctx, `
		INSERT INTO commands (id, router_id, command_type, payload, status, sent_at, created_at)
		VALUES 
		($1, $2, $3, $4, $5, $6, $7),
		($8, $9, $10, $11, $12, $13, $14)
	`,
		command1ID, routerID, pb.CommandType_REBOOT, `{"param1": "value1"}`, pb.Status_PENDING, time.Now(), time.Now().Add(-10*time.Minute),
		command2ID, routerID, pb.CommandType_PING, `{"param2": "value2"}`, pb.Status_PENDING, time.Now(), time.Now().Add(-5*time.Minute),
	)
	assert.NoError(suite.T(), err)

	req := &pb.PollRequest{
		RouterId: routerID.String(),
	}

	// Act
	response, err := suite.controller.Poll(suite.ctx, req)

	// Assert
	assert.NoError(suite.T(), err)
	assert.NotNil(suite.T(), response)
	assert.Len(suite.T(), response.Commands, 2)

	for _, cmd := range response.Commands {
		assert.NotEmpty(suite.T(), cmd.Id)
		assert.Contains(suite.T(), []pb.CommandType{pb.CommandType_REBOOT, pb.CommandType_PING}, cmd.CommandType)
	}
}

func (suite *CommandControllerIntegrationTestSuite) TestPoll_EmptyResult() {
	// Arrange
	req := &pb.PollRequest{
		RouterId: uuid.New().String(),
	}

	// Act
	response, err := suite.controller.Poll(suite.ctx, req)

	// Assert
	assert.NoError(suite.T(), err)
	assert.NotNil(suite.T(), response)
	assert.Empty(suite.T(), response.Commands)
}

func (suite *CommandControllerIntegrationTestSuite) TestAck_Success() {
	// Arrange
	routerID := uuid.New()
	serialNumber := "RT-" + routerID.String()[:8]
	commandID := uuid.New()

	db := suite.pgContainer.GetDB()

	_, err := db.ExecContext(suite.ctx, `
		INSERT INTO routers (id, serial_number, last_seen_at, created_at)
		VALUES ($1, $2, $3, $4)
	`,
		routerID, serialNumber, time.Now(), time.Now(),
	)
	assert.NoError(suite.T(), err)

	_, err = db.ExecContext(suite.ctx, `
		INSERT INTO commands (id, router_id, command_type, payload, status, sent_at, created_at)
		VALUES ($1, $2, $3, $4, $5, $6, $7)
	`, commandID, routerID, "REBOOT", `{"param": "value"}`, "SENT", time.Now(), time.Now())
	assert.NoError(suite.T(), err)

	req := &pb.AckRequest{
		RouterId:  routerID.String(),
		CommandId: commandID.String(),
	}

	// Act
	response, err := suite.controller.Ack(suite.ctx, req)

	// Assert
	assert.NoError(suite.T(), err)
	assert.NotNil(suite.T(), response)
	assert.IsType(suite.T(), &emptypb.Empty{}, response)

	var (
		dbStatus    string
		ackTime     sql.NullTime
		dbRouterID  uuid.UUID
		dbCommandID uuid.UUID
	)
	err = db.QueryRowContext(suite.ctx,
		`SELECT status, acked_at, router_id, id 
		 FROM commands WHERE id = $1`,
		commandID).Scan(&dbStatus, &ackTime, &dbRouterID, &dbCommandID)

	assert.NoError(suite.T(), err)
	assert.Equal(suite.T(), "ACKED", dbStatus)
	assert.True(suite.T(), ackTime.Valid)
	assert.WithinDuration(suite.T(), time.Now(), ackTime.Time, 5*time.Second)
	assert.Equal(suite.T(), routerID, dbRouterID)
	assert.Equal(suite.T(), commandID, dbCommandID)
}

func (suite *CommandControllerIntegrationTestSuite) TestAck_CommandAlreadyAcked() {
	// Arrange
	routerID := uuid.New()
	serialNumber := "RT-" + routerID.String()[:8]
	commandID := uuid.New()

	db := suite.pgContainer.GetDB()

	_, err := db.ExecContext(suite.ctx, `
		INSERT INTO routers (id, serial_number, last_seen_at, created_at)
		VALUES ($1, $2, $3, $4)
	`,
		routerID, serialNumber, time.Now(), time.Now(),
	)
	assert.NoError(suite.T(), err)

	_, err = db.ExecContext(suite.ctx, `
		INSERT INTO commands (id, router_id, command_type, payload, status, sent_at, acked_at, created_at)
		VALUES ($1, $2, $3, $4, $5, $6, $7, $8)
	`, commandID, routerID, "REBOOT", `{"param": "value"}`,
		"ACKED", time.Now().Add(-10*time.Minute), time.Now().Add(-5*time.Minute), time.Now())
	assert.NoError(suite.T(), err)

	req := &pb.AckRequest{
		RouterId:  routerID.String(),
		CommandId: commandID.String(),
	}

	// Act
	response, err := suite.controller.Ack(suite.ctx, req)

	// Assert
	if err != nil {
		assert.Contains(suite.T(), err.Error(), "already")
	} else {
		assert.NotNil(suite.T(), response)
	}
}

func (suite *CommandControllerIntegrationTestSuite) TestAck_MultipleCommands() {
	// Arrange
	routerID := uuid.New()
	serialNumber := "RT-" + routerID.String()[:8]

	db := suite.pgContainer.GetDB()

	_, err := db.ExecContext(suite.ctx, `
		INSERT INTO routers (id, serial_number, last_seen_at, created_at)
		VALUES ($1, $2, $3, $4)
	`,
		routerID, serialNumber, time.Now(), time.Now(),
	)
	assert.NoError(suite.T(), err)

	commandIDs := []uuid.UUID{uuid.New(), uuid.New(), uuid.New()}
	for i, cmdID := range commandIDs {
		_, err = db.ExecContext(suite.ctx, `
			INSERT INTO commands (id, router_id, command_type, payload, status, sent_at, created_at)
			VALUES ($1, $2, $3, $4, $5, $6, $7)
		`, cmdID, routerID, "PING", fmt.Sprintf(`{"seq": %d}`, i),
			"SENT", time.Now().Add(time.Duration(i)*time.Minute), time.Now())
		assert.NoError(suite.T(), err)
	}

	for _, cmdID := range commandIDs {
		req := &pb.AckRequest{
			RouterId:  routerID.String(),
			CommandId: cmdID.String(),
		}

		response, err := suite.controller.Ack(suite.ctx, req)
		assert.NoError(suite.T(), err)
		assert.NotNil(suite.T(), response)
	}

	var ackedCount int
	err = db.QueryRowContext(suite.ctx,
		"SELECT COUNT(*) FROM commands WHERE router_id = $1 AND status = $2",
		routerID, "ACKED").Scan(&ackedCount)
	assert.NoError(suite.T(), err)
	assert.Equal(suite.T(), 3, ackedCount)

	var nullAckCount int
	err = db.QueryRowContext(suite.ctx,
		"SELECT COUNT(*) FROM commands WHERE router_id = $1 AND acked_at IS NULL",
		routerID).Scan(&nullAckCount)
	assert.NoError(suite.T(), err)
	assert.Equal(suite.T(), 0, nullAckCount)
}
