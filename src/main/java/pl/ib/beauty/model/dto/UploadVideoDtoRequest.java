package pl.ib.beauty.model.dto;

public record UploadVideoDtoRequest(String filename,
                                    String contentType,
                                    String title,
                                    String description) {
}
