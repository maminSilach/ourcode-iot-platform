package service

import (
	"log"
	"router-manager/internal/model"
	pb "router-manager/internal/proto/api/v1"
	"router-manager/internal/repository"

	"github.com/google/uuid"
	"google.golang.org/grpc/codes"
	"google.golang.org/grpc/status"
)

type CommandService struct {
	repo repository.CommandRepositoryInterface
}

func NewCommandService(repo repository.CommandRepositoryInterface) CommandService {
	return CommandService{repo: repo}
}

func (s *CommandService) CreateCommand(req *pb.CommandRequest, status pb.Status) error {
	if err := s.validateCommandRequest(req); err != nil {
		log.Printf("Error processing validate request: %v", err)
		return err
	}

	commandToSave, err := model.ToCommandModel(req, status)
	if err != nil {
		log.Printf("Error processing dto mapping: %v", err)
		return err
	}

	if err := s.repo.CreateCommand(commandToSave); err != nil {
		log.Printf("Error processing create command: %v", err)
		return err
	}

	return nil
}

func (s *CommandService) PollCommands(req *pb.PollRequest) (*pb.PollResponse, error) {
	if err := s.validatePollRequest(req); err != nil {
		log.Printf("Error processing validate request: %v", err)
		return nil, err
	}

	commands, err := s.repo.PollCommands(req.GetRouterId())

	if err != nil {
		log.Printf("Error processing poll commands: %v", err)
		return nil, err
	}

	pollResponse := &pb.PollResponse{Commands: commands}

	return pollResponse, nil
}

func (s *CommandService) AckCommand(req *pb.AckRequest) error {
	if err := s.validateAckRequest(req); err != nil {
		log.Printf("Error processing validate request: %v", err)
		return err
	}

	err := s.repo.AckCommand(req.GetRouterId(), req.GetCommandId())

	if err != nil {
		log.Printf("Error processing ack command: %v", err)
		return err
	}

	return err
}

func (s *CommandService) validateCommandRequest(req *pb.CommandRequest) error {
	commandType := req.GetCommandType()

	if _, exists := pb.CommandType_name[int32(commandType)]; !exists {
		return status.Error(codes.InvalidArgument, "invalid command_type")
	}

	if req.GetRouterId() != "" {
		if _, err := uuid.Parse(req.GetRouterId()); err != nil {
			return status.Errorf(codes.InvalidArgument, "incorrect format router_id: %v", err)
		}
	}

	return nil
}

func (s *CommandService) validatePollRequest(req *pb.PollRequest) error {
	if req.GetRouterId() == "" {
		return status.Error(codes.InvalidArgument, "router_id must have a value")
	}

	return nil
}

func (s *CommandService) validateAckRequest(req *pb.AckRequest) error {
	if req.GetCommandId() == "" {
		return status.Error(codes.InvalidArgument, "command_id must have a value")
	}

	if req.GetRouterId() == "" {
		return status.Error(codes.InvalidArgument, "router_id must have a value")
	}

	return nil
}
