package pl.ib.beauty.validator.impl;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.io.FilenameUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public class ExtensionValidator implements ConstraintValidator<ExtensionValid, MultipartFile> {
    private List<String> extensions;

    @Override
    public void initialize(ExtensionValid constraintAnnotation) {
        extensions = List.of(constraintAnnotation.supportedExtensions());
    }

    @Override
    public boolean isValid(MultipartFile multipartFile, ConstraintValidatorContext constraintValidatorContext) {
        return extensions.contains(FilenameUtils.getExtension(multipartFile.getOriginalFilename()));
    }


}
