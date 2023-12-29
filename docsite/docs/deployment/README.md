# Deployment

Whitefox is developed using Quarkus framework, therefore its natural habitat is a containerized environment.

Due to dependencies upon Apache Hadoop, it's not possible to create GrallVM native images of whitefox (for now) 
so right now, you need a JVM.  

For your convenience, we publish on [ghcr](https://github.com/orgs/agile-lab-dev/packages?repo_name=whitefox) container
images at each push on the main branch, therefore you can pick the runtime of your choice,
pull the image from the registry, and you're good to go.

To make things even easier, we will collect in this section, ready to go guides in order to deploy whitefox.

Right now we feature the following platforms:
- [Amazon lightsail](lightsail.md)