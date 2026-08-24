package com.equipmentrental.maintenance.dto.response;

import com.equipmentrental.maintenance.enums.AttachmentType;

import java.time.LocalDateTime;

public record AttachmentResponse(

        Long id,
        Long workOrderId,
        Long organizationId,
        Long branchId,
        AttachmentType attachmentType,
        String fileName,
        String fileUrl,
        String mimeType,
        Long fileSizeBytes,
        String description,
        Long uploadedByUserId,
        LocalDateTime uploadedAt
) {
}