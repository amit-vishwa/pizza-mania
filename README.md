# Pizza Mania Backend

Pizza Mania is a Spring Boot learning application that models products, carts, purchases, and users while demonstrating authentication and role-based authorization with local JWTs and AWS Cognito.

## Features

- User registration and sign-in
- BCrypt password hashing and JWT authentication
- AWS Cognito integration for external users
- User, product, cart, purchase, and purchase-detail APIs
- Role-based authorization for users, managers, and administrators
- JPA persistence with MySQL
- Search, sorting, filtering, and pagination utilities
- Postman collection and database DDL reference files

## Requirements

- JDK 17
- MySQL
- AWS credentials and a Cognito user pool for Cognito-backed operations

## Local configuration

Copy `.env.example` to a local `.env` file and replace every placeholder. Spring Boot does not automatically load `.env`; export the values through your shell, IDE, container runtime, or another local environment loader before starting the application.

The application requires database, JWT, and Cognito settings listed in `.env.example`. Never commit real secrets or AWS credentials.

Optional demo accounts are disabled by default. To create `user@example.com`, `manager@example.com`, and `admin@example.com` for local testing, set `BOOTSTRAP_USERS_ENABLED=true` and provide a local-only `BOOTSTRAP_USERS_PASSWORD` containing at least 12 characters. Do not enable these accounts in a public or production deployment.

Run the application with:

```powershell
.\mvnw.cmd spring-boot:run
```

The Postman collection and additional application notes are under `src/main/resources`. Use only synthetic test data.

## Security notes

- API request and response bodies are retained for the existing audit trail after recursive masking of password, token, authorization, and secret fields. Payloads that cannot be safely parsed and masked are not stored.
- Authorization headers and bearer tokens must never be written to application logs.
- Audit URLs retain the endpoint path but omit query strings, which may contain credentials under arbitrary parameter names. Request/response JSON payload logging remains enabled with recursive sensitive-field masking.
- Audit persistence failures produce a fixed warning without exception messages or stack traces, which could contain SQL parameters or payload values.
- The project is a learning application and has not been reviewed or operated as a production service.
