# Gateway Service

The gateway service is the prime microservice that
serves as the entry point for the LOS application. 

All API calls
that will be made to this service will be done by making it from 
the Gateway service itself.

Along with api, Gateway service also offers several actuators and health
metric helpers, as well as traceability for distributed tracing.

## Setup (Local)

If you wish to run this project locally, you just need to create an `application-local.yaml`
from the provided `application-local.txt` in the `src/main/resources` folder.

After that run the project as usual, by using `GatewayServiceApplication.main()` function.

## Setup (Docker)

To setup the project using Docker, do the following:

- In the `src/main/resources` folder, create a file called `application-docker.yaml` using the provided
`application-docker.txt`. 

Fill in the variables as needed. Note that the hosts correspond to the 
Docker container names.

- Next, run the command from the root directory of the project:
```bash 
docker-compose up --build -d
```
This will run the build, and after some time if all variables are adjusted correctly,
then you can see the success message as well!

## Supported APIs 

To be updated