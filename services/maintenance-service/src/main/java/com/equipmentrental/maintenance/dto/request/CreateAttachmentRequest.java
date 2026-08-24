package com.equipmentrental.maintenance.dto.request;

import com.equipmentrental.maintenance.enums.AttachmentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateAttachmentRequest(

        @NotNull
        AttachmentType attachmentType,

        String fileName,

        @NotBlank
        String fileUrl,

        String mimeType,

        Long fileSizeBytes,

        String description
) {
}