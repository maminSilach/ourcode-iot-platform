package grpc

import (
	"context"
	"log"
	_ "net"
	"router-manager/internal/service"

	_ "google.golang.org/grpc"
	"google.golang.org/protobuf/types/known/emptypb"

	_ "github.com/jackc/pgx/v5"

	pb "router-manager/internal/proto/api/v1"
)

type CommandServiceController struct {
	pb.UnimplementedCommandServiceServer
	service service.CommandService
}

func NewCommandServiceController(service service.CommandService) *CommandServiceController {
	return &CommandServiceController{service: service}
}

func (s *CommandServiceController) SendCommand(_ context.Context, req *pb.CommandRequest) (*emptypb.Empty, error) {
	log.Printf("Received request: router_id=%s, command_type=%s", req.GetRouterId(), req.GetCommandType())

	err := s.service.CreateCommand(req, pb.Status_PENDING)
	if err != nil {
		return nil, err
	}

	return &emptypb.Empty{}, nil
}

func (s *CommandServiceController) Poll(_ context.Context, req *pb.PollRequest) (*pb.PollResponse, error) {
	log.Printf("Received request for user with ID: %s", req.GetRouterId())

	commands, err := s.service.PollCommands(req)
	if err != nil {
		return nil, err
	}

	return commands, nil
}

func (s *CommandServiceController) Ack(_ context.Context, req *pb.AckRequest) (*emptypb.Empty, error) {
	log.Printf("Received request for user with ID: %s", req.GetRouterId())

	err := s.service.AckCommand(req)
	if err != nil {
		return nil, err
	}

	return &emptypb.Empty{}, nil
}
