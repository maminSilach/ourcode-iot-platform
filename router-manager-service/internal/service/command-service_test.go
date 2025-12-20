package service

import (
	"errors"
	"router-manager/internal/model"
	pb "router-manager/internal/proto/api/v1"
	_ "router-manager/internal/repository"
	"testing"

	"github.com/google/uuid"
	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/mock"
	"google.golang.org/grpc/codes"
	"google.golang.org/grpc/status"
)

type MockCommandRepository struct {
	mock.Mock
}

func (m *MockCommandRepository) CreateCommand(commandToSave *model.CommandModel) error {
	args := m.Called(commandToSave)
	return args.Error(0)
}

func (m *MockCommandRepository) PollCommands(routerID string) ([]*pb.Command, error) {
	args := m.Called(routerID)
	return args.Get(0).([]*pb.Command), args.Error(1)
}

func (m *MockCommandRepository) AckCommand(routerID, commandID string) error {
	args := m.Called(routerID, commandID)
	return args.Error(0)
}

func TestCommandService_CreateCommand_Success(t *testing.T) {
	// Arrange
	mockRepo := new(MockCommandRepository)
	service := NewCommandService(mockRepo)

	routerUUID := uuid.New()

	req := &pb.CommandRequest{
		RouterId:    routerUUID.String(),
		CommandType: pb.CommandType_REBOOT,
	}

	mockRepo.On("CreateCommand", mock.AnythingOfType("*model.CommandModel")).Return(nil)

	// Act
	err := service.CreateCommand(req, pb.Status_PENDING)

	// Assert
	assert.NoError(t, err)
	mockRepo.AssertCalled(t, "CreateCommand", mock.AnythingOfType("*model.CommandModel"))
}

func TestCommandService_CreateCommand_InvalidCommandType(t *testing.T) {
	// Arrange
	mockRepo := new(MockCommandRepository)
	service := NewCommandService(mockRepo)

	req := &pb.CommandRequest{
		RouterId:    uuid.New().String(),
		CommandType: pb.CommandType(999),
	}

	err := service.CreateCommand(req, pb.Status_PENDING)

	// Assert
	assert.Error(t, err)
	assert.Equal(t, codes.InvalidArgument, status.Code(err))
	mockRepo.AssertNotCalled(t, "CreateCommand")
}

func TestCommandService_CreateCommand_InvalidRouterID(t *testing.T) {
	// Arrange
	mockRepo := new(MockCommandRepository)
	service := NewCommandService(mockRepo)

	req := &pb.CommandRequest{
		RouterId:    "invalid-uuid",
		CommandType: pb.CommandType_PING,
	}

	// Act
	err := service.CreateCommand(req, pb.Status_PENDING)

	// Assert
	assert.Error(t, err)
	assert.Equal(t, codes.InvalidArgument, status.Code(err))
	mockRepo.AssertNotCalled(t, "CreateCommand")
}

func TestCommandService_CreateCommand_RepositoryError(t *testing.T) {
	// Arrange
	mockRepo := new(MockCommandRepository)
	service := NewCommandService(mockRepo)

	validUUID := uuid.New().String()
	req := &pb.CommandRequest{
		RouterId:    validUUID,
		CommandType: pb.CommandType_REBOOT,
	}

	expectedError := errors.New("database error")
	mockRepo.On("CreateCommand", mock.AnythingOfType("*model.CommandModel")).Return(expectedError)

	// Act
	err := service.CreateCommand(req, pb.Status_PENDING)

	// Assert
	assert.Error(t, err)
	assert.Equal(t, expectedError, err)
}

func TestCommandService_PollCommands_Success(t *testing.T) {
	// Arrange
	mockRepo := new(MockCommandRepository)
	service := NewCommandService(mockRepo)

	routerID := uuid.New().String()
	req := &pb.PollRequest{RouterId: routerID}

	expectedCommands := []*pb.Command{
		{
			Id:          uuid.New().String(),
			CommandType: pb.CommandType_REBOOT,
		},
		{
			Id:          uuid.New().String(),
			CommandType: pb.CommandType_PING,
		},
	}

	mockRepo.On("PollCommands", routerID).Return(expectedCommands, nil)

	// Act
	response, err := service.PollCommands(req)

	// Assert
	assert.NoError(t, err)
	assert.NotNil(t, response)
	assert.Equal(t, expectedCommands, response.Commands)
	mockRepo.AssertCalled(t, "PollCommands", routerID)
}

func TestCommandService_PollCommands_EmptyRouterID(t *testing.T) {
	// Arrange
	mockRepo := new(MockCommandRepository)
	service := NewCommandService(mockRepo)

	req := &pb.PollRequest{RouterId: ""}

	expectedCommands := []*pb.Command{
		{
			Id:          uuid.New().String(),
			CommandType: pb.CommandType_REBOOT,
		},
		{
			Id:          uuid.New().String(),
			CommandType: pb.CommandType_PING,
		},
	}

	mockRepo.On("PollCommands", "").Return(expectedCommands, nil)

	// Act
	response, err := service.PollCommands(req)

	// Assert
	assert.NoError(t, err)
	assert.NotNil(t, response)
	assert.Equal(t, expectedCommands, response.Commands)
	mockRepo.AssertCalled(t, "PollCommands", "")
}

func TestCommandService_PollCommands_RepositoryError(t *testing.T) {
	// Arrange
	mockRepo := new(MockCommandRepository)
	service := NewCommandService(mockRepo)

	routerID := uuid.New().String()
	req := &pb.PollRequest{RouterId: routerID}

	expectedError := errors.New("database error")
	mockRepo.On("PollCommands", routerID).Return([]*pb.Command{}, expectedError)

	// Act
	response, err := service.PollCommands(req)

	// Assert
	assert.Error(t, err)
	assert.Nil(t, response)
	assert.Equal(t, expectedError, err)
	mockRepo.AssertCalled(t, "PollCommands", routerID)
}

func TestCommandService_PollCommands_EmptyResponse(t *testing.T) {
	// Arrange
	mockRepo := new(MockCommandRepository)
	service := NewCommandService(mockRepo)

	routerID := uuid.New().String()
	req := &pb.PollRequest{RouterId: routerID}

	mockRepo.On("PollCommands", routerID).Return([]*pb.Command{}, nil)

	// Act
	response, err := service.PollCommands(req)

	// Assert
	assert.NoError(t, err)
	assert.NotNil(t, response)
	assert.Empty(t, response.Commands)
	mockRepo.AssertCalled(t, "PollCommands", routerID)
}

func TestCommandService_AckCommand_Success(t *testing.T) {
	// Arrange
	mockRepo := new(MockCommandRepository)
	service := NewCommandService(mockRepo)

	routerID := uuid.New().String()
	commandID := uuid.New().String()
	req := &pb.AckRequest{
		RouterId:  routerID,
		CommandId: commandID,
	}

	mockRepo.On("AckCommand", routerID, commandID).Return(nil)

	// Act
	err := service.AckCommand(req)

	// Assert
	assert.NoError(t, err)
	mockRepo.AssertCalled(t, "AckCommand", routerID, commandID)
}

func TestCommandService_AckCommand_EmptyCommandID(t *testing.T) {
	// Arrange
	mockRepo := new(MockCommandRepository)
	service := NewCommandService(mockRepo)

	req := &pb.AckRequest{
		RouterId:  uuid.New().String(),
		CommandId: "",
	}

	// Act
	err := service.AckCommand(req)

	// Assert
	assert.Error(t, err)
	assert.Equal(t, codes.InvalidArgument, status.Code(err))
	mockRepo.AssertNotCalled(t, "AckCommand")
}

func TestCommandService_AckCommand_EmptyRouterID(t *testing.T) {
	// Arrange
	mockRepo := new(MockCommandRepository)
	service := NewCommandService(mockRepo)

	req := &pb.AckRequest{
		RouterId:  "",
		CommandId: uuid.New().String(),
	}

	// Act
	err := service.AckCommand(req)

	// Assert
	assert.Error(t, err)
	assert.Equal(t, codes.InvalidArgument, status.Code(err))
	mockRepo.AssertNotCalled(t, "AckCommand")
}

func TestCommandService_AckCommand_RepositoryError(t *testing.T) {
	// Arrange
	mockRepo := new(MockCommandRepository)
	service := NewCommandService(mockRepo)

	routerID := uuid.New().String()
	commandID := uuid.New().String()
	req := &pb.AckRequest{
		RouterId:  routerID,
		CommandId: commandID,
	}

	expectedError := errors.New("ack error")
	mockRepo.On("AckCommand", routerID, commandID).Return(expectedError)

	// Act
	err := service.AckCommand(req)

	// Assert
	assert.Error(t, err)
	assert.Equal(t, expectedError, err)
	mockRepo.AssertCalled(t, "AckCommand", routerID, commandID)
}

func TestNewCommandService(t *testing.T) {
	// Arrange
	mockRepo := new(MockCommandRepository)

	// Act
	service := NewCommandService(mockRepo)

	// Assert
	assert.NotNil(t, service)
	assert.Equal(t, mockRepo, service.repo)
}
