package grpc

import (
	"context"
	"log"
	_ "net"

	_ "google.golang.org/grpc"
	"google.golang.org/protobuf/types/known/emptypb"

	pb "url-shortener/internal/proto/api/v1" // Путь к сгенерированным файлам Protobuf
)

type CommandServiceServer struct {
	pb.UnimplementedCommandServiceServer
}

func (s *CommandServiceServer) SendCommand(ctx context.Context, req *pb.CommandRequest) (*emptypb.Empty, error) {
	log.Printf("Получен запрос на пользователя с ID: %d", req.GetRouterId())

	// Пример данных
	return &emptypb.Empty{}, nil
}

func (s *CommandServiceServer) Poll(ctx context.Context, req *pb.PollRequest) (*pb.PollResponse, error) {
	log.Printf("Получен запрос на пользователя с ID: %d", req.GetRouterId())

	// Пример данных
	return nil, nil
}

func (s *CommandServiceServer) Ack(ctx context.Context, req *pb.AckRequest) (*emptypb.Empty, error) {
	log.Printf("Получен запрос на пользователя с ID: %d", req.GetRouterId())

	// Пример данных
	return &emptypb.Empty{}, nil
}
