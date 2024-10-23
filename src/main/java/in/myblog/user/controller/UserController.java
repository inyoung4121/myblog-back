package in.myblog.user.controller;

import in.myblog.post.dto.VerifyAuthResponse;
import in.myblog.user.dto.*;
import in.myblog.user.service.UserService;
import in.myblog.user.domain.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "User Management", description = "APIs for managing users")
@RequiredArgsConstructor
@RequestMapping("/api")
@RestController
public class UserController {

    private final UserService userService;

    @Operation(summary = "Register a new user", description = "Creates a new user account"+
            "A refresh token is set in an HTTP-only secure cookie named 'my_blog_refresh_token'.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully logged in",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ResponseUserDTO.class)),
                    headers = {
                            @Header(name = "Authorization", description = "Bearer token for authentication",
                                    schema = @Schema(type = "string"))
                    }),
            @ApiResponse(responseCode = "400", description = "Invalid input",
                    content = @Content),
            @ApiResponse(responseCode = "409", description = "User already exists",
                    content = @Content)
    })
    @PostMapping("/signup")
    public ResponseEntity<ResponseUserDTO> registerUser(@RequestBody RequestSignupUserDTO request, HttpServletResponse response) {
        ResponseUserDTO responseDTO = userService.registerUser(request.getUsername(), request.getEmail(), request.getPassword(), response);
        return ResponseEntity.ok(responseDTO);
    }

    @Operation(summary = "Login user", description = "Authenticates a user and returns user details. " +
            "A refresh token is set in an HTTP-only secure cookie named 'my_blog_refresh_token'.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully logged in",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ResponseUserDTO.class)),
                    headers = {
                            @Header(name = "Authorization", description = "Bearer token for authentication",
                                    schema = @Schema(type = "string"))
                    }),
            @ApiResponse(responseCode = "401", description = "Invalid credentials",
                    content = @Content)
    })
    @PostMapping("/login")
    public ResponseEntity<ResponseUserDTO> login(@RequestBody RequestLoginDTO request, HttpServletResponse response) {
        ResponseUserDTO responseDTO = userService.login(request.getEmail(), request.getPassword(), response);
        return ResponseEntity.ok(responseDTO);
    }


    @Operation(summary = "Change user password", description = "Updates the password for a user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Password successfully changed"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PutMapping("/secure/password")
    public ResponseEntity<Void> changePassword(@RequestBody RequestChangePasswordDTO request) {
        Long userId = Long.valueOf(((Users) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername());
        userService.changePassword(userId, request.getOldPassword(), request.getNewPassword());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Change username", description = "Updates the username for a user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully logged in",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ResponseUserDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PutMapping("/secure/username")
    public ResponseEntity<ResponseUserDTO> changeUsername(@RequestBody RequestChangeNameDTO request, HttpServletResponse response) {
        Long userId = Long.valueOf(((Users) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername());
        Users user = userService.changeUsername(userId, request.getNewUserName());
        ResponseUserDTO responseDTO = new ResponseUserDTO(user);
        return ResponseEntity.ok(responseDTO);
    }

    @Operation(summary = "Request role change", description = "Submits a request for role change")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Role change request submitted"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PostMapping("/secure/role-change-request")
    public ResponseEntity<RoleChangeRequest> requestRoleChange() {
        Long userId = Long.valueOf(((Users) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername());
        RoleChangeRequest request = userService.requestRoleChange(userId);
        return ResponseEntity.ok(request);
    }

    @Operation(summary = "Approve role change request", description = "Approves a pending role change request")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Role change request approved"),
            @ApiResponse(responseCode = "404", description = "Request not found")
    })
    @PutMapping("/secure/role-change-request/{requestId}/approve")
    public ResponseEntity<Void> approveRoleChangeRequest(@PathVariable Long requestId) {
        userService.approveRoleChangeRequest(requestId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Reject role change request", description = "Rejects a pending role change request")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Role change request rejected"),
            @ApiResponse(responseCode = "404", description = "Request not found")
    })
    @PutMapping("/secure/role-change-request/{requestId}/reject")
    public ResponseEntity<Void> rejectRoleChangeRequest(@PathVariable Long requestId) {
        userService.rejectRoleChangeRequest(requestId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Get all role change requests", description = "Retrieves a list of all role change requests")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list"),
            @ApiResponse(responseCode = "403", description = "Accessing the resource you were trying to reach is forbidden")
    })
    @GetMapping("/secure/role-change-requests")
    public ResponseEntity<List<RoleChangeRequest>> getAllRoleChangeRequests() {
        List<RoleChangeRequest> requests = userService.getAllRoleChangeRequests();
        return ResponseEntity.ok(requests);
    }

    @GetMapping("/verify-auth")
    public ResponseEntity<?> verifyAuth(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            if (userDetails == null) {
                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body(new VerifyAuthResponse(false, null));
            }

            // 사용자의 권한 중 첫 번째 권한을 role로 사용
            String role = userDetails.getAuthorities().stream()
                    .findFirst()
                    .map(authority -> authority.getAuthority().replace("ROLE_", ""))
                    .orElse(null);

            return ResponseEntity.ok(new VerifyAuthResponse(
                    true,
                    role
            ));

        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new VerifyAuthResponse(false, null));
        }
    }
}