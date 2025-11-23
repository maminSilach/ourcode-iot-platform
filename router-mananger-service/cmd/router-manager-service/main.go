package main

import (
	"log"
	"net"
	cs "url-shortener/internal/grpc"
	pb "url-shortener/internal/proto/api/v1"

	"google.golang.org/grpc"
)

func main() {
	listen, err := net.Listen("tcp", ":52281")
	if err != nil {
		log.Fatal(err)
	}

	grpcServer := grpc.NewServer()
	pb.RegisterCommandServiceServer(grpcServer, &cs.CommandServiceServer{})

	log.Println("gRPC сервер запущен на :52281")
	if err := grpcServer.Serve(listen); err != nil {
		log.Fatal(err)
	}
}

//  protoc --go_out=./ --go-grpc_out=./ router-manager-service.proto
//  goose -dir database/migrations postgres "postgresql://postgres:postgres@127.0.0.1:5433/dev?sslmode=disable" up
