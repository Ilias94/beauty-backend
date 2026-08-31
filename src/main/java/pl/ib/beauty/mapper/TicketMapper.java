package pl.ib.beauty.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pl.ib.beauty.model.dao.Ticket;
import pl.ib.beauty.model.dao.TicketReply;
import pl.ib.beauty.model.dao.TicketResolution;
import pl.ib.beauty.model.dto.TicketDto;
import pl.ib.beauty.model.dto.TicketReplyDto;
import pl.ib.beauty.model.dto.TicketResolutionDto;

@Mapper(componentModel = "spring")
public interface TicketMapper {

    @Mapping(target = "authorId", source = "author.id")
    @Mapping(target = "authorName", expression = "java(ticket.getAuthor().getFirstName() + \" \" + ticket.getAuthor().getLastName())")
    @Mapping(target = "assignedToId", source = "assignedTo.id")
    @Mapping(target = "assignedToName", expression = "java(ticket.getAssignedTo() != null ? ticket.getAssignedTo().getFirstName() + \" \" + ticket.getAssignedTo().getLastName() : null)")
    TicketDto toDto(Ticket ticket);

    TicketReplyDto replyToDto(TicketReply reply);

    TicketResolutionDto resolutionToDto(TicketResolution resolution);
}
