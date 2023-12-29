provider "aws" {
  region = var.region
}
variable "region" {
  type = string
}
resource "aws_lightsail_container_service" "whitefox-container-service" {
  name        = "whitefox-container-service"
  power       = "nano"
  scale       = 1
  is_disabled = false
}

resource "aws_lightsail_container_service_deployment_version" "example" {
  container {
    container_name = "whitefox-server"
    image          = "ghcr.io/agile-lab-dev/io.whitefox.server:latest"

    command = []

    environment = {
    }

    ports = {
      8080 = "HTTP"
    }
  }

  public_endpoint {
    container_name = "whitefox-server"
    container_port = 8080

    health_check {
      healthy_threshold   = 2
      unhealthy_threshold = 2
      timeout_seconds     = 2
      interval_seconds    = 5
      path                = "/q/health/live"
      success_codes       = "200"
    }
  }

  service_name = aws_lightsail_container_service.whitefox-container-service.name
}