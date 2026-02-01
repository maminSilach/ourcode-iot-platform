package grpc

import (
	"fmt"
	"log"
	"net"
	"router-manager/internal/config"
	pb "router-manager/internal/proto/api/v1"

	"google.golang.org/grpc"
)

type GrpcServer struct {
	config                   *config.Config
	commandServiceController *CommandServiceController
}

func New(cfg *config.Config, controller *CommandServiceController) *GrpcServer {
	return &GrpcServer{
		config:                   cfg,
		commandServiceController: controller,
	}
}

func (s *GrpcServer) Run() {
	port := fmt.Sprintf(":%d", s.config.Server.Port)

	listen, err := net.Listen("tcp", port)
	if err != nil {
		log.Fatal(err)
	}

	grpcServer := grpc.NewServer()
	pb.RegisterCommandServiceServer(grpcServer, s.commandServiceController)

	log.Println("gRPC server started")
	if err := grpcServer.Serve(listen); err != nil {
		log.Fatal(err)
	}
}
