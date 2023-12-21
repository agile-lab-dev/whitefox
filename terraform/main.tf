provider "aws" {
  region = var.region
}

data "aws_availability_zones" "available" {
  state = "available"
}

variable "public_subnet" {
  type    = list(string)
  default = []
}

variable "region" {
  type = string
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
resource "aws_subnet" "whitefox_public_subnets" {
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

resource "aws_ecs_cluster" "ecs_cluster" {
  name = "whitefox-demo-cluster"
}

resource "aws_ecs_task_definition" "whitefox_server" {
  family                   = "whitefox-server"
  network_mode             = "awsvpc"
  requires_compatibilities = ["FARGATE"]
  cpu                      = "256"
  memory                   = "512"
  execution_role_arn       = aws_iam_role.whitefox_execution_role.arn
  container_definitions = <<DEFINITION
  [
    {
      "name": "whitefox",
      "image": "ghcr.io/agile-lab-dev/io.whitefox.server:latest",
      "cpu": 256,
      "portMappings": [
        {
          "containerPort": 8080,
          "hostPort": 8080,
          "protocol": "TCP"
        }
      ],
      "essential": true,
      "logConfiguration": {
        "logDriver": "awslogs",
        "options": {
          "awslogs-group": "${aws_cloudwatch_log_group.whitefox_server_log_group.name}",
          "awslogs-region": "${var.region}",
          "awslogs-stream-prefix": "whitefox-prefix"
        }
      }
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
    assign_public_ip = true
    subnets          = [for subnet in aws_subnet.whitefox_public_subnets : subnet.id]
    security_groups  = [aws_security_group.whitefox_sg.id]
  }
  desired_count = 1
}

resource "aws_iam_role" "whitefox_execution_role" {
  name = "whitefox-server-execution-role"

  assume_role_policy = jsonencode({
    Version   = "2012-10-17",
    Statement = [
      {
        Action    = "sts:AssumeRole",
        Effect    = "Allow",
        Principal = {
          Service = "ecs-tasks.amazonaws.com",
        },
      },
    ],
  })
}

resource "aws_iam_policy" "whitefox-cloudwatch_logs_policy" {
  name        = "whitefox-cloudwatch-logs-policy"
  description = "Allows Fargate to write logs to CloudWatch Logs"

  policy = jsonencode({
    Version   = "2012-10-17",
    Statement = [
      {
        Action = [
          "logs:CreateLogGroup",
          "logs:CreateLogStream",
          "logs:PutLogEvents",
        ],
        Effect   = "Allow",
        Resource = "*",
      },
    ],
  })
}

resource "aws_iam_role_policy_attachment" "attach_cloudwatch_logs_policy" {
  policy_arn = aws_iam_policy.whitefox-cloudwatch_logs_policy.arn
  role       = aws_iam_role.whitefox_execution_role.name
}
resource "aws_cloudwatch_log_group" "whitefox_server_log_group" {
  name = "/ecs/whitefox"

  retention_in_days = 1
}