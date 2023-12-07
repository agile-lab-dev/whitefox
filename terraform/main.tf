provider "aws" {
  region = "eu-west-1"
}
data "aws_availability_zones" "available" {
  state = "available"
}
variable "public_subnet" {
  type    = list(string)
  default = []
}
resource "aws_vpc" "whitefox_vpc" {
  cidr_block           = "10.0.0.0/16"
  enable_dns_support   = true
  enable_dns_hostnames = true
  tags                 = {
    Name = "whitefox-vpc"
  }
}
resource "aws_internet_gateway" "whitefox_igv" {
  vpc_id = "${aws_vpc.whitefox_vpc.id}"
  tags   = {
    Name = "whitefox_igv"
  }
}

resource "aws_subnet" "whitefox_public_subnets" {
  count             = "${length(var.public_subnet)}"
  vpc_id            = "${aws_vpc.whitefox_vpc.id}"
  cidr_block        = "${var.public_subnet[count.index]}"
  availability_zone = "${data.aws_availability_zones.available.names[count.index]}"
  tags              = {
    Name = "whitefox-public-subnet-${count.index}"
  }
  map_public_ip_on_launch = true  # This is important for instances in the subnet to get a public IP
}

# Create a route table for public subnets
resource "aws_route_table" "whitefox_public" {
  vpc_id = aws_vpc.whitefox_vpc.id

  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_internet_gateway.whitefox_igv.id
  }

  tags = {
    Name = "public"
  }
}

# Associate public subnets with the public route table
resource "aws_route_table_association" "public_association" {
  count          = length(aws_subnet.whitefox_public_subnets)
  subnet_id      = aws_subnet.whitefox_public_subnets[count.index].id
  route_table_id = aws_route_table.whitefox_public.id
}

####
resource "aws_subnet" "whitefox_subnets" {

  count             = "${length(var.public_subnet)}"
  vpc_id            = "${aws_vpc.whitefox_vpc.id}"
  cidr_block        = "${var.public_subnet[count.index]}"
  availability_zone = "${data.aws_availability_zones.available.names[count.index]}"
  tags              = {
    Name = "whitefox-subnet-${count.index}"
  }
}

resource "aws_security_group" "whitefox_sg" {
  name        = "whitefox-security-group"
  description = "Allow incoming HTTP traffic"
  vpc_id      = aws_vpc.whitefox_vpc.id

  ingress {
    from_port   = 8080
    to_port     = 8080
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}

resource "aws_security_group" "whitefox_lb_sg" {
  name        = "whitefox-lb-security-group"
  description = "Allow incoming traffic to the Load Balancer"
  vpc_id      = aws_vpc.whitefox_vpc.id

  ingress {
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}

resource "aws_ecs_cluster" "ecs_cluster" {
  name = "whitefox-demo-cluster"
}

resource "aws_ecs_task_definition" "whitefox_server" {
  family                   = "whitefox-server"
  network_mode             = "awsvpc"
  requires_compatibilities = ["FARGATE"]
  cpu                      = "256"
  memory                   = "512"

  container_definitions = <<DEFINITION
  [
    {
      "name": "whitefox",
      "image": "ghcr.io/agile-lab-dev/io.whitefox.server:latest",
      "cpu": 256,
      "portMappings": [
        {
          "containerPort": 8080,
          "hostPort": 8080
        }
      ]
    }
  ]
  DEFINITION
}
resource "aws_ecs_service" "whitefox_service" {
  name            = "whitefox-service"
  cluster         = aws_ecs_cluster.ecs_cluster.id
  task_definition = aws_ecs_task_definition.whitefox_server.id
  launch_type     = "FARGATE"

  network_configuration {
    subnets         = [for subnet in aws_subnet.whitefox_public_subnets : subnet.id]
    security_groups = [aws_security_group.whitefox_sg.id]
  }

  load_balancer {
    target_group_arn = aws_lb_target_group.whitefox_target_group.arn
    container_name = "whitefox"
    container_port = 8080
  }

  depends_on = [aws_ecs_cluster.ecs_cluster, aws_ecs_task_definition.whitefox_server, aws_lb_listener.whitefox_listener]
}

resource "aws_lb" "whitefox_lb" {
  name               = "whitefox-lb"
  internal           = false
  load_balancer_type = "application"
  security_groups    = [aws_security_group.whitefox_lb_sg.id]
  subnets            = [for subnet in aws_subnet.whitefox_public_subnets : subnet.id]
}

resource "aws_lb_listener" "whitefox_listener" {
  load_balancer_arn = aws_lb.whitefox_lb.arn
  port              = 80
  protocol          = "HTTP"

  default_action {
    target_group_arn = aws_lb_target_group.whitefox_target_group.arn
    type             = "forward"
  }
}
resource "aws_lb_target_group" "whitefox_target_group" {
  name     = "whitefox-target-group"
  port     = 8080
  protocol = "HTTP"
  target_type = "ip"

  vpc_id = aws_vpc.whitefox_vpc.id
}
