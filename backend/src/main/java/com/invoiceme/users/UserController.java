package com.invoiceme.users;

import com.invoiceme.users.approveuser.ApproveUserCommand;
import com.invoiceme.users.approveuser.ApproveUserHandler;
import com.invoiceme.users.getpendingusers.GetPendingUsersQuery;
import com.invoiceme.users.getpendingusers.GetPendingUsersHandler;
import com.invoiceme.users.getpendingusers.PendingUserDto;
import com.invoiceme.users.rejectuser.RejectUserCommand;
import com.invoiceme.users.rejectuser.RejectUserHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    
    private final GetPendingUsersHandler getPendingUsersHandler;
    private final ApproveUserHandler approveUserHandler;
    private final RejectUserHandler rejectUserHandler;
    
    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('SYSADMIN', 'ACCOUNTANT')")
    public ResponseEntity<List<PendingUserDto>> getPendingUsers() {
        GetPendingUsersQuery query = new GetPendingUsersQuery();
        List<PendingUserDto> response = getPendingUsersHandler.handle(query);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('SYSADMIN', 'ACCOUNTANT')")
    public ResponseEntity<Void> approveUser(@PathVariable UUID id) {
        // TODO: Get approvedByUserId from security context
        ApproveUserCommand command = new ApproveUserCommand(id, UUID.randomUUID());
        approveUserHandler.handle(command);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
    
    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('SYSADMIN', 'ACCOUNTANT')")
    public ResponseEntity<Void> rejectUser(
            @PathVariable UUID id,
            @RequestBody(required = false) String reason) {
        // TODO: Get rejectedByUserId from security context
        RejectUserCommand command = new RejectUserCommand(id, reason != null ? reason : "Not specified", UUID.randomUUID());
        rejectUserHandler.handle(command);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}

