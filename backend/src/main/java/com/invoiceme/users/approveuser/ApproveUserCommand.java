package com.invoiceme.users.approveuser;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApproveUserCommand {
    private UUID userId;
    private UUID approvedByUserId;
}

