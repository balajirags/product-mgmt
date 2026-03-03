```
GOAL: Create REST controller for ⟪AggregateName⟫ using DTOs only.
CONTEXT: 
- Expose only REST API endpoints.
- Always use snake_case in api request and response
- Do not implement business logic in controllers.
- Only call the corresponding service method for each endpoint.
- Use appropriate HTTP status codes.
- Use validation annotations on DTOs.
- Handle exceptions via @RestControllerAdvice (not in controller).
- Follow clean code principles.
- Follow security best practices for input validation and output encoding.
- Use consistent and meaningful logging.
- Write Javadoc comments for public methods.
- Use Controller advice for cross-cutting concerns error handling.
- Use dependency injection for services.
[../instructions/clean-code.instructions.md](../instructions/clean-code.instructions.md) for best practices.
[../instructions/security-owasp.instructions.md](../instructions/security-owasp.instructions.md) for security.
[../instructions/logging.instructions.md](../instructions/logging.instructions.md) for logging.
ENDPOINTS: POST /⟪resource⟫, GET /⟪resource⟫/{id}, GET /⟪resource⟫ (paged search), PUT/PATCH for updates.
RULES: No entities in API; map exceptions via @RestControllerAdvice.
GENERATE NOW
```