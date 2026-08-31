package pl.ib.beauty.mapper;

import org.mapstruct.Mapper;
import pl.ib.beauty.model.dao.Faq;
import pl.ib.beauty.model.dto.FaqDtoRequest;
import pl.ib.beauty.model.dto.FaqDtoResponse;

import java.util.List;

@Mapper(componentModel = "spring")
public interface FaqMapper {

    FaqDtoResponse faqToFaqDto(Faq faq);

    List<FaqDtoResponse> faqsToFaqDtos(List<Faq> faqs);

    Faq faqDtoToFaq(FaqDtoRequest faqDtoRequest);
}
